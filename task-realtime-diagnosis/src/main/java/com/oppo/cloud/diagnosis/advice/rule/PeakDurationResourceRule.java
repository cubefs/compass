package com.oppo.cloud.diagnosis.advice.rule;

import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Lists;
import com.oppo.cloud.common.domain.flink.enums.EDiagnosisReportYAxisType;
import com.oppo.cloud.common.domain.flink.metric.MetricResult;
import com.oppo.cloud.common.domain.flink.report.DiagnosisRuleLine;
import com.oppo.cloud.common.domain.flink.report.DiagnosisRuleLineChart;
import com.oppo.cloud.common.domain.flink.report.DiagnosisRuleReport;
import com.oppo.cloud.diagnosis.advice.BaseRule;
import com.oppo.cloud.diagnosis.advice.turning.TurningAdvice;
import com.oppo.cloud.diagnosis.advice.turning.TurningManager;
import com.oppo.cloud.diagnosis.constant.DiagnosisParamsConstants;
import com.oppo.cloud.common.domain.flink.enums.EDiagnosisRule;
import com.oppo.cloud.common.domain.flink.enums.EDiagnosisParam;
import com.oppo.cloud.common.domain.flink.enums.EDiagnosisTurningStatus;
import com.oppo.cloud.diagnosis.domain.diagnosis.DiagnosisContext;
import com.oppo.cloud.diagnosis.domain.diagnosis.RcJobDiagnosisAdvice;
import com.oppo.cloud.diagnosis.util.DoctorUtil;
import com.oppo.cloud.diagnosis.util.MonitorMetricUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.oppo.cloud.diagnosis.constant.MonitorMetricConstant.TM_CPU_USAGE_RATE;


/**
 * 监测峰值时候的资源情况，判断是否需要扩容
 */
@Slf4j
@Component
public class PeakDurationResourceRule extends BaseRule {
    @Autowired
    DiagnosisParamsConstants cons;
    @Autowired
    DoctorUtil doctorUtil;
    @Autowired
    TurningManager turningManager ;
    @Autowired
    MonitorMetricUtil monitorMetricUtil ;
    @Override
    public RcJobDiagnosisAdvice advice(DiagnosisContext context) {
        Double cpuHighThreshold = doctorUtil.getCpuHighThreshold(context);
        Double cpuHighTarget = doctorUtil.getCpuHighTarget(context);
        RcJobDiagnosisAdvice.RcJobDiagnosisAdviceBuilder builder = getBuilder(context);
        builder.adviceType(EDiagnosisRule.PeekDurationResourceRule);
        // 判断cpu峰值利用高
        // 拿到每个tm的cpu使用率
        List<MetricResult.DataResult> cpuUsageList = context.getMetrics().get(TM_CPU_USAGE_RATE);
        if (cpuUsageList == null || cpuUsageList.size() == 0) {
            return builder
                    .hasAdvice(false)
                    .adviceDescription("tm cpu usage为空")
                    .build();
        }
        for (MetricResult.DataResult dataResult : cpuUsageList) {
            try {
                // 最近的五分钟cpu 高于阈值,取累计时间还是取均值,取均值,要消尖，不取平滑，平滑会减少点数
                long end = context.getEnd();
                long minutes5Before = end - cons.getTmCpuHighLatestNMinutes() * 60;
                Supplier<Stream<MetricResult.KeyValue>> flatMetric = monitorMetricUtil.getFlatKeyValueStream(dataResult);
                OptionalDouble averageUnitCpuMinutes5Before = flatMetric.get()
                        .filter(x -> {
                                    if (x == null) {
                                        return false;
                                    }
                                    if (x.getTs() == null) {
                                        return false;
                                    }
                                    return x.getTs() >= minutes5Before;
                                }
                        )
                        .map(MetricResult.KeyValue::getValue)
                        .mapToDouble(x -> x)
                        .average();
                if (averageUnitCpuMinutes5Before.isPresent()
                        && averageUnitCpuMinutes5Before.getAsDouble() > cpuHighThreshold) {
                    log.debug("tm 最近{} 分钟 cpu均值高于阈值:" + context.getRcJobDiagnosis().getJobName() + ":" + JSON.toJSONString(dataResult),
                            cons.getTmCpuHighLatestNMinutes());
                    log.info(String.format("作业部分tm 最近%d 分钟 cpu均值利用率:%.2f%% 超过%.2f%%",
                            cons.getTmCpuHighLatestNMinutes(), averageUnitCpuMinutes5Before.getAsDouble() * 100,
                            cpuHighThreshold.floatValue() * 100));
                    Double changeRate = averageUnitCpuMinutes5Before.getAsDouble() / cpuHighTarget - 1;
                    context.getMessages().put(EDiagnosisParam.GrowCpuChangeRate, changeRate);
                    TurningAdvice turning = turningManager.turningCpuUp(context);
                    if (turning != null && turning.getStatus().equals(EDiagnosisTurningStatus.HAS_ADVICE)) {
                        RcJobDiagnosisAdvice build = convertTurningToAdviceBuilder(turning,builder)
                                .hasAdvice(true)
                                .adviceDescription(String.format("作业部分TM最近%d分钟CPU均值利用率:%.2f%%,超过%.2f%%",
                                        cons.getTmCpuHighLatestNMinutes(), averageUnitCpuMinutes5Before.getAsDouble() * 100,
                                        cpuHighThreshold.floatValue() * 100)
                                ).build();
                        convertAdviceToRcJobDiagnosis(build,context);
                        String resourceChange = buildResourceChange(context);
                        String conclusion = String.format("作业部分TM最近%d分钟CPU均值利用率:%.2f%%,超过%.2f%%,%s",
                                cons.getTmCpuHighLatestNMinutes(), averageUnitCpuMinutes5Before.getAsDouble() * 100,
                                cpuHighThreshold.floatValue() * 100,resourceChange);
                        DiagnosisRuleReport diagnosisRuleReport = new DiagnosisRuleReport();
                        diagnosisRuleReport.setTitle("峰值CPU利用率高分析");
                        diagnosisRuleReport.setConclusion(conclusion);
                        DiagnosisRuleLineChart diagnosisRuleLineChart = new DiagnosisRuleLineChart();
                        diagnosisRuleLineChart.setTitle("作业CPU使用率");
                        diagnosisRuleLineChart.setYAxisUnit("%");
                        diagnosisRuleLineChart.setYAxisValueType(EDiagnosisReportYAxisType.Percent.name());
                        diagnosisRuleLineChart.setYAxisMax(1d);
                        diagnosisRuleLineChart.setYAxisMin(0d);
                        DiagnosisRuleLine line = new DiagnosisRuleLine();
                        line.setLabel("作业CPU使用率");
                        line.setData(cpuUsageList);
                        diagnosisRuleLineChart.setLine(line);
                        Map<String,Double> constLine = new HashMap<>();
                        constLine.put("阈值",cpuHighThreshold);
                        constLine.put("最近五分钟CPU均值",averageUnitCpuMinutes5Before.getAsDouble());
                        diagnosisRuleLineChart.setConstLines(constLine);
                        diagnosisRuleReport.setIDiagnosisRuleCharts(Lists.newArrayList(diagnosisRuleLineChart));
                        build.setDiagnosisRuleReport(diagnosisRuleReport);
                        return build;
                    } else {
                        String desc = String.format("作业部分tm 最近%d 分钟 cpu均值利用率:%.2f%% 超过%.2f%%",
                                cons.getTmCpuHighLatestNMinutes(), averageUnitCpuMinutes5Before.getAsDouble() * 100,
                                cpuHighThreshold.floatValue() * 100);
                        if (turning != null) {
                            desc = desc + "," + turning.getDescription();
                        }
                        return builder
                                .hasAdvice(false)
                                .adviceDescription(desc)
                                .build();
                    }
                }

                // 全程累计时间高于阈值
                int step = monitorMetricUtil.getStep(context.getStart(), context.getEnd());
                Supplier<Stream<MetricResult.KeyValue>> smoothMetric = monitorMetricUtil.getSmoothKeyValueStream(
                        monitorMetricUtil.getFlatKeyValueStream(dataResult), 3);
                List<Double> collectHighValue = smoothMetric.get()
                        .map(MetricResult.KeyValue::getValue)
                        .filter(cpuUsage -> {
                            if (cpuUsage == null) {
                                return false;
                            }
                            if (cpuUsage > cpuHighThreshold.floatValue()) {
                                return true;
                            }
                            return false;
                        }).collect(Collectors.toList());
                long highDuration = (long) collectHighValue.size() * step;
                if (highDuration > cons.getTmPeakHighTimeThreshold()) {
                    log.debug("tm单个峰值利用率高的数据:" + context.getRcJobDiagnosis().getJobName() + ":" + JSON.toJSONString(dataResult));
                    TurningAdvice turning = turningManager.turningCpuUp(context);
                    if (turning != null && turning.getStatus().equals(EDiagnosisTurningStatus.HAS_ADVICE)) {
                        RcJobDiagnosisAdvice build = convertTurningToAdviceBuilder(turning,builder)
                                .hasAdvice(true)
                                .adviceDescription(String.format("作业部分tm峰值归一化cpu利用率超过%.2f%%的时间累计超过%.2f秒",
                                        cpuHighThreshold.floatValue() * 100, cons.getTmPeakHighTimeThreshold())
                                ).build();
                        convertAdviceToRcJobDiagnosis(build,context);
                        String resourceChange = buildResourceChange(context);
                        String conclusion = String.format("作业部分tm峰值归一化cpu利用率超过%.2f%%的时间累计超过%.2f秒,%s",
                                cpuHighThreshold.floatValue() * 100, cons.getTmPeakHighTimeThreshold(),resourceChange);
                        DiagnosisRuleReport diagnosisRuleReport = new DiagnosisRuleReport();
                        diagnosisRuleReport.setTitle("峰值CPU利用率高分析");
                        diagnosisRuleReport.setConclusion(conclusion);
                        DiagnosisRuleLineChart diagnosisRuleLineChart = new DiagnosisRuleLineChart();
                        diagnosisRuleLineChart.setTitle("作业CPU使用率");
                        diagnosisRuleLineChart.setYAxisUnit("%");
                        diagnosisRuleLineChart.setYAxisValueType(EDiagnosisReportYAxisType.Percent.name());
                        diagnosisRuleLineChart.setYAxisMax(1d);
                        diagnosisRuleLineChart.setYAxisMin(0d);
                        DiagnosisRuleLine line = new DiagnosisRuleLine();
                        line.setLabel("作业CPU使用率");
                        line.setData(cpuUsageList);
                        diagnosisRuleLineChart.setLine(line);
                        Map<String,Double> constLine = new HashMap<>();
                        constLine.put("阈值",cpuHighThreshold);
                        diagnosisRuleLineChart.setConstLines(constLine);
                        diagnosisRuleReport.setIDiagnosisRuleCharts(Lists.newArrayList(diagnosisRuleLineChart));
                        build.setDiagnosisRuleReport(diagnosisRuleReport);
                        return build;
                    } else {
                        String desc = String.format("作业部分tm峰值cpu利用率高于%.2f%%", cpuHighThreshold.floatValue() * 100);
                        if (turning != null) {
                            desc = desc + "," + turning.getDescription();
                        }
                        return builder
                                .hasAdvice(false)
                                .adviceDescription(desc)
                                .build();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("cpu峰值利用率高调优报错" + e.getMessage());
            }
        }

        return builder
                .hasAdvice(false)
                .adviceDescription("无建议")
                .build();
    }

}
