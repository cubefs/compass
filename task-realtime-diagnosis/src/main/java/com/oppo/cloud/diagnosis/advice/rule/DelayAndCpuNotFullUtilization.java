package com.oppo.cloud.diagnosis.advice.rule;


import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Lists;
import com.oppo.cloud.common.domain.flink.enums.EDiagnosisReportYAxisType;
import com.oppo.cloud.common.domain.flink.report.DiagnosisRuleLine;
import com.oppo.cloud.common.domain.flink.report.DiagnosisRuleLineChart;
import com.oppo.cloud.common.domain.flink.report.DiagnosisRuleReport;
import com.oppo.cloud.diagnosis.advice.BaseRule;
import com.oppo.cloud.diagnosis.advice.turning.MemTurningByUsage;
import com.oppo.cloud.diagnosis.advice.turning.TurningAdvice;
import com.oppo.cloud.diagnosis.constant.DiagnosisParamsConstants;
import com.oppo.cloud.common.domain.flink.enums.EDiagnosisRule;
import com.oppo.cloud.diagnosis.domain.diagnosis.DiagnosisContext;
import com.oppo.cloud.diagnosis.domain.diagnosis.RcJobDiagnosis;
import com.oppo.cloud.diagnosis.domain.diagnosis.RcJobDiagnosisAdvice;
import com.oppo.cloud.diagnosis.service.impl.RealtimeDiagnosisMetricsServiceImpl;
import com.oppo.cloud.common.domain.flink.metric.MetricResult;
import com.oppo.cloud.diagnosis.util.DoctorUtil;
import com.oppo.cloud.diagnosis.util.MonitorMetricUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.oppo.cloud.diagnosis.constant.MonitorMetricConstant.*;


/**
 * 如果有延迟，并且core数大于单 tm slot总数，且cpu利用率上不去，就增大slot 数(+1)，并且同时按比例增加并行度，并行度不超过cpu。
 */
@Component
@Slf4j
public class DelayAndCpuNotFullUtilization extends BaseRule {
    @Autowired
    DiagnosisParamsConstants cons;
    @Autowired
    DoctorUtil doctorUtil;
    @Resource
    MonitorMetricUtil monitorMetricUtil;
    @Autowired
    RealtimeDiagnosisMetricsServiceImpl realtimeDiagnosisMetricsServiceImpl;
    @Autowired
    MemTurningByUsage memTurningByUsage;

    @Override
    public RcJobDiagnosisAdvice advice(DiagnosisContext context) {
        RcJobDiagnosis rcJobDiagnosis = context.getRcJobDiagnosis();
        RcJobDiagnosisAdvice.RcJobDiagnosisAdviceBuilder builder = getBuilder(context);
        builder.adviceType(EDiagnosisRule.ParallelGrow);
        // 如果任务有延迟,延迟需要持续大于10分钟,需要消尖
        List<MetricResult.DataResult> delayTimeLagList = context.getMetrics().get(MAX_TIME_LAG_PROMQL);
        if (delayTimeLagList == null || delayTimeLagList.size() == 0) {
            return builder
                    .adviceDescription("delay time 为空")
                    .build();
        }
        long delayHighCount = monitorMetricUtil.getFlatKeyValueStream(delayTimeLagList.get(0))
                .get()
                .filter(Objects::nonNull)
                .filter(x -> {
                    return (x.getValue() != null && x.getValue() > cons.JOB_CUT_LAG_TIME_THRESHOLD);
                })
                .count();
        double maxDelay = monitorMetricUtil.getFlatKeyValueStream(delayTimeLagList.get(0))
                .get()
                .map(MetricResult.KeyValue::getValue)
                .filter(Objects::nonNull)
                .mapToDouble(x -> x)
                .max()
                .orElse(0d);
        int step = monitorMetricUtil.getStep(context.getStart(), context.getEnd());
        int countThreshold = (int) Math.ceil(10d * 60d / step);
        boolean isDelay = (delayHighCount > countThreshold);
        Boolean delayLittleHigh = (maxDelay > cons.JOB_DELAY_LITTLE_HIGH);
        log.info("{} {}-{} 作业最大延迟 {} second", rcJobDiagnosis.getJobName(), context.getStart(),
                context.getEnd(), maxDelay);
        // 判断最近10分钟内延迟连续
        Boolean offsetGrow10minutes = offsetGrow10minutes(context);
        Double cpuHighThreshold = doctorUtil.getCpuHighThreshold(context);
        boolean cpuNotHigh = notNullLt(rcJobDiagnosis.getTmAvgCpuUsageAvg() / rcJobDiagnosis.getTmCore(),
                cpuHighThreshold.floatValue());
        Boolean slotLtCpu = rcJobDiagnosis.getTmSlotNum() < rcJobDiagnosis.getTmCore();
        int maxParallel = cons.maxParallel;
        Integer sourcePartitionNumObj = rcJobDiagnosis.getKafkaConsumePartitionNum();
        if (sourcePartitionNumObj == null) {
            log.info("{} 拿不到source partition num", rcJobDiagnosis.getJobName());
        } else {
            maxParallel = 4 * sourcePartitionNumObj;
        }
        if ((isDelay || (offsetGrow10minutes && delayLittleHigh)) && cpuNotHigh) {
            // 增加并行度,调整cpu，调整内存
            int oriSlotNum = rcJobDiagnosis.getTmSlotNum();
            int newSlotNum = oriSlotNum;
            int oriTmMem = rcJobDiagnosis.getTmMem();
            int newTmMem = oriTmMem;

            // 调整 slot number,使得内存足够
            while (newSlotNum > 1) {
                // 计算内存是否符合要求
                TurningAdvice newMemAdvice = memTurningByUsage.turning(context, newSlotNum);
                if (newMemAdvice == null || newMemAdvice.getTmMem() == null) {
                    newTmMem = oriTmMem * (int) Math.floor((double) newSlotNum / oriSlotNum);
                    newTmMem = newTmMem / 1024 * 1024;
                } else {
                    newTmMem = newMemAdvice.getTmMem();
                }
                // 内存调整要尽量保守，防止oom
                if (newTmMem + 1024 <= cons.tmMemMax) {
                    newTmMem = newTmMem + 1024;
                }
                if (newTmMem >= cons.tmMemMin && newTmMem <= cons.tmMemMax) {
                    break;
                }
                newSlotNum--;
            }
            if (newSlotNum < 1) {
                newSlotNum = 1;
            }
            if (newTmMem > cons.tmMemMax || newTmMem < cons.tmMemMin) {
                newTmMem = cons.tmMemMax;
            }
            // 如果并行度小于最大并行度,增加并行度
            int newParallel = rcJobDiagnosis.getParallel();
            if (rcJobDiagnosis.getParallel() < maxParallel) {
                int iterateParallel = maxParallel;
                while ((int) Math.ceil((double) iterateParallel / 2) > rcJobDiagnosis.getParallel()) {
                    iterateParallel = (int) Math.ceil((double) iterateParallel / 2);
                }
                newParallel = iterateParallel;
            } else {
                // 并行度已经最大了，那么尝试减少slot num,增加tm个数
                if (newSlotNum >= oriSlotNum && oriSlotNum > 1) {
                    newSlotNum = oriSlotNum - 1;
                }
            }
            // cpu 设置等于slot
            int newCpu = newSlotNum;
            int newTmNum = (int) Math.ceil((double) newParallel / newSlotNum);
            int oriTmNum = rcJobDiagnosis.getTmNum();
            int oriCpu = rcJobDiagnosis.getTmCore();
            int oriParallel = rcJobDiagnosis.getParallel();
            if (
                    newTmNum != oriTmNum ||
                            newCpu != oriCpu ||
                            newParallel != oriParallel ||
                            newTmMem != oriTmMem ||
                            newSlotNum != oriSlotNum
            ) {

                RcJobDiagnosisAdvice build = builder.hasAdvice(true)
                        .diagnosisTmNum(newTmNum)
                        .diagnosisTmCore(newCpu)
                        .diagnosisParallel(newParallel)
                        .diagnosisTmMem(newTmMem)
                        .diagnosisTmSlotNum(newSlotNum)
                        .adviceDescription(
                                String.format("作业出现延迟%.2f second,且cpu平均利用率%.2f%%不超过阈值%.2f%%",
                                        maxDelay, rcJobDiagnosis.getTmAvgCpuUsageAvg() / rcJobDiagnosis.getTmCore() * 100, cpuHighThreshold * 100))
                        .build();
                convertAdviceToRcJobDiagnosis(build, context);
                String resourceChange = buildResourceChange(context);
                String conclusion = String.format("作业出现延迟%.2f second,且cpu平均利用率%.2f%%不超过阈值%.2f%%,%s",
                        maxDelay, rcJobDiagnosis.getTmAvgCpuUsageAvg() / rcJobDiagnosis.getTmCore() * 100, cpuHighThreshold * 100, resourceChange);
                DiagnosisRuleReport diagnosisRuleReport = new DiagnosisRuleReport();
                diagnosisRuleReport.setTitle("并行度不足分析");
                diagnosisRuleReport.setConclusion(conclusion);
                DiagnosisRuleLineChart diagnosisRuleLineChartDelay = new DiagnosisRuleLineChart();
                diagnosisRuleLineChartDelay.setTitle("作业延迟");
                diagnosisRuleLineChartDelay.setYAxisUnit("(秒)");
                diagnosisRuleLineChartDelay.setYAxisValueType(EDiagnosisReportYAxisType.Second.name());
                DiagnosisRuleLine line = new DiagnosisRuleLine();
                line.setLabel("作业延迟");
                line.setData(delayTimeLagList);
                diagnosisRuleLineChartDelay.setLine(line);
                Map<String, Double> constLine = new HashMap<>();
                constLine.put("阈值", cons.JOB_DELAY_LITTLE_HIGH.doubleValue());
                diagnosisRuleLineChartDelay.setConstLines(constLine);
                DiagnosisRuleLineChart diagnosisRuleLineChartCpu = new DiagnosisRuleLineChart();
                diagnosisRuleLineChartCpu.setTitle("作业CPU使用率");
                diagnosisRuleLineChartCpu.setYAxisUnit("(%)");
                diagnosisRuleLineChartCpu.setYAxisValueType(EDiagnosisReportYAxisType.Percent.name());
                DiagnosisRuleLine cpuLine = new DiagnosisRuleLine();
                List<MetricResult.DataResult> cpuUsageList = context.getMetrics().get(TM_AVG_CPU_USAGE_RATE);
                cpuLine.setLabel("作业CPU使用率");
                cpuLine.setData(cpuUsageList);
                diagnosisRuleLineChartCpu.setLine(cpuLine);
                Map<String, Double> constLineCpu = new HashMap<>();
                constLineCpu.put("阈值", cpuHighThreshold);
                constLineCpu.put("均值", (double) (rcJobDiagnosis.getTmAvgCpuUsageAvg() / rcJobDiagnosis.getTmCore()));
                diagnosisRuleLineChartCpu.setConstLines(constLineCpu);
                diagnosisRuleReport.setIDiagnosisRuleCharts(Lists.newArrayList(diagnosisRuleLineChartDelay,
                        diagnosisRuleLineChartCpu));
                build.setDiagnosisRuleReport(diagnosisRuleReport);
                return build;
            } else {
                return builder
                        .adviceDescription(
                                String.format("作业出现延迟%.2f second,且cpu最大利用率%.2f%%不超过阈值%.2f%%,没有合适参数",
                                        maxDelay,
                                        rcJobDiagnosis.getTmAvgCpuUsageAvg() / rcJobDiagnosis.getTmCore() * 100, cpuHighThreshold * 100))
                        .build();
            }

        } else {
            log.info("{} GrowSlotRule isDelay:{} offsetGrow10minutes:{} cpuNotHigh:{} slotLtCpu:{}",
                    rcJobDiagnosis.getJobName(), isDelay, offsetGrow10minutes, cpuNotHigh, slotLtCpu);
        }
        return builder
                .adviceDescription("无建议")
                .build();
    }

    /**
     * 判断作业最近10minute offset 连续上涨
     *
     * @param context
     * @return
     */
    private boolean offsetGrow10minutes(DiagnosisContext context) {
        List<MetricResult.DataResult> offsetDeltaMetrics = realtimeDiagnosisMetricsServiceImpl.getTaskManagerMetrics(OFFSET_DELTA, context, context.getStart(), context.getEnd());
        if (offsetDeltaMetrics == null || offsetDeltaMetrics.size() != 1) {
            log.info("{} job offset delta 列表数量不为1", context.getRcJobDiagnosis().getJobName());
            return false;
        }
        // 获取延迟开始上涨的那个点
        Optional<Integer> ts = monitorMetricUtil.getKeyValueStream(offsetDeltaMetrics.get(0))
                .get()
                .map(MetricResult.KeyValue::getTs)
                .max(Integer::compareTo);
        if (!ts.isPresent()) {
            log.error("{} 获取ts失败", context.getRcJobDiagnosis().getJobName());
            return false;
        }
        Integer maxTs = ts.get();
        List<MetricResult.KeyValue> latest10minOffsetDelta = monitorMetricUtil.getKeyValueStream(offsetDeltaMetrics.get(0))
                .get()
                .filter(x -> {
                    return x.getTs() > maxTs - 10 * 60;
                })
                .collect(Collectors.toList());
        Optional<Double> minOffsetDelta = latest10minOffsetDelta
                .stream()
                .map(MetricResult.KeyValue::getValue)
                .min(Double::compareTo);
        if (!minOffsetDelta.isPresent()) {
            log.error("{} 获取offset delta 失败", context.getRcJobDiagnosis().getJobName());
            return false;
        }
        if (minOffsetDelta.get() > 0) {
            log.info("{} 连续10分钟offset 上涨 {}", context.getRcJobDiagnosis().getJobName(),
                    latest10minOffsetDelta);
            return true;
        } else {
            log.info("{} 没有连续10分钟offset 上涨 {}", context.getRcJobDiagnosis().getJobName(),
                    latest10minOffsetDelta);
            return false;
        }
    }

}
