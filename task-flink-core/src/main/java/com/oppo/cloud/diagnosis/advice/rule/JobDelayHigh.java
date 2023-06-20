package com.oppo.cloud.diagnosis.advice.rule;

import com.google.common.collect.Lists;
import com.oppo.cloud.common.domain.flink.enums.DiagnosisReportYAxisType;
import com.oppo.cloud.common.domain.flink.report.DiagnosisRuleLine;
import com.oppo.cloud.common.domain.flink.report.DiagnosisRuleLineChart;
import com.oppo.cloud.common.domain.flink.report.DiagnosisRuleReport;
import com.oppo.cloud.diagnosis.advice.BaseRule;
import com.oppo.cloud.diagnosis.advice.turning.MemTurningByUsage;
import com.oppo.cloud.diagnosis.constant.DiagnosisParamsConstants;
import com.oppo.cloud.common.domain.flink.enums.FlinkRule;
import com.oppo.cloud.diagnosis.domain.diagnosis.DiagnosisContext;
import com.oppo.cloud.diagnosis.domain.diagnosis.RcJobDiagnosis;
import com.oppo.cloud.diagnosis.domain.diagnosis.RcJobDiagnosisAdvice;
import com.oppo.cloud.diagnosis.service.impl.FlinkDiagnosisMetricsServiceImpl;
import com.oppo.cloud.common.domain.flink.metric.MetricResult;
import com.oppo.cloud.diagnosis.util.DoctorUtil;
import com.oppo.cloud.diagnosis.util.MonitorMetricUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.oppo.cloud.diagnosis.constant.MonitorMetricConstant.MAX_TIME_LAG_PROMQL;
import static com.oppo.cloud.diagnosis.constant.MonitorMetricConstant.OFFSET_DELTA;

@Component
@Slf4j
public class JobDelayHigh extends BaseRule {
    @Autowired
    DiagnosisParamsConstants cons;
    @Autowired
    DoctorUtil doctorUtil;
    @Resource
    MonitorMetricUtil monitorMetricUtil;
    @Autowired
    FlinkDiagnosisMetricsServiceImpl flinkDiagnosisMetricsServiceImpl;
    @Autowired
    MemTurningByUsage memTurningByUsage;

    @Override
    public RcJobDiagnosisAdvice advice(DiagnosisContext context) {
        RcJobDiagnosis rcJobDiagnosis = context.getRcJobDiagnosis();
        RcJobDiagnosisAdvice.RcJobDiagnosisAdviceBuilder builder = getBuilder(context);
        builder.adviceType(FlinkRule.JobDelay);
        // 如果任务有延迟,延迟需要持续大于10分钟,需要消尖
        List<MetricResult.DataResult> delayTimeLagList = context.getMetrics().get(MAX_TIME_LAG_PROMQL);
        if (delayTimeLagList == null || delayTimeLagList.size() == 0) {
            return builder
                    .adviceDescription("delay time 为空")
                    .build();
        }
        long delayHighCount = monitorMetricUtil.getFlatKeyValueStream(delayTimeLagList.get(0))
                .get()
                .filter(x -> {
                    return x.getValue() > cons.JOB_CUT_LAG_TIME_THRESHOLD;
                })
                .count();
        double latestDelay = monitorMetricUtil.getLatestOrNull(delayTimeLagList);
        double maxDelay = monitorMetricUtil.getMax(delayTimeLagList);
        int step = monitorMetricUtil.getStep(context.getStart(), context.getEnd());
        int countThreshold = (int) Math.ceil(10d * 60d / step);
        boolean isDelay = (delayHighCount > countThreshold);
        Boolean delayLittleHigh = (latestDelay > cons.JOB_DELAY_LITTLE_HIGH);
        log.debug("{} {}-{} 作业最近延迟 {} second", rcJobDiagnosis.getJobName(), context.getStart(),
                context.getEnd(), latestDelay);
        // 判断最近10分钟内延迟连续
        Boolean offsetGrow10minutes = offsetGrow10minutes(context);
        Double cpuHighThreshold = doctorUtil.getCpuHighThreshold(context);
        boolean cpuNotHigh = notNullLt(rcJobDiagnosis.getTmAvgCpuUsageAvg() / rcJobDiagnosis.getTmCore(),
                cpuHighThreshold.floatValue());
        if ((isDelay || (offsetGrow10minutes && delayLittleHigh))) {
            builder.hasAdvice(true)
                    .adviceDescription(
                            String.format("作业出现延迟%.2f second,建议检查资源设置情况以及查看日志是否有报错", maxDelay));
            String conclusion = String.format("诊断周期内作业延迟大于阈值10分钟累计持续10分钟,或者最近10分钟延迟上涨，任务延迟");
            DiagnosisRuleReport diagnosisRuleReport = new DiagnosisRuleReport();
            diagnosisRuleReport.setTitle("作业消费延迟分析");
            diagnosisRuleReport.setConclusion(conclusion);
            DiagnosisRuleLineChart diagnosisRuleLineChart = new DiagnosisRuleLineChart();
            diagnosisRuleLineChart.setTitle("作业消费延迟");
            diagnosisRuleLineChart.setYAxisUnit("(秒)");
            diagnosisRuleLineChart.setYAxisValueType(DiagnosisReportYAxisType.Second.name());
            DiagnosisRuleLine line = new DiagnosisRuleLine();
            line.setLabel("作业消费延迟");
            line.setData(delayTimeLagList);
            diagnosisRuleLineChart.setLine(line);
            Map<String, Double> constLine = new HashMap<>();
            constLine.put("阈值", cons.JOB_DELAY_LITTLE_HIGH.doubleValue());
            diagnosisRuleLineChart.setConstLines(constLine);
            diagnosisRuleReport.setIDiagnosisRuleCharts(Lists.newArrayList(diagnosisRuleLineChart));
            builder.diagnosisRuleReport(diagnosisRuleReport);
            return builder.build();
        } else {
            log.info("{} GrowSlotRule isDelay:{} offsetGrow10minutes:{} cpuNotHigh:{}",
                    rcJobDiagnosis.getJobName(), isDelay, offsetGrow10minutes, cpuNotHigh);
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
        List<MetricResult.DataResult> offsetDeltaMetrics = flinkDiagnosisMetricsServiceImpl
                .getTaskManagerMetrics(OFFSET_DELTA, context,context.getStart(), context.getEnd());
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
            log.debug("{} 连续10分钟offset 上涨 {}", context.getRcJobDiagnosis().getJobName(),
                    latest10minOffsetDelta);
            return true;
        } else {
            log.debug("{} 没有连续10分钟offset 上涨 {}", context.getRcJobDiagnosis().getJobName(),
                    latest10minOffsetDelta);
            return false;
        }
    }
}
