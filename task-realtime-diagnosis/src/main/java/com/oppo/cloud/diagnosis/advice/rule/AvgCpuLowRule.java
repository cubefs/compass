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
import com.oppo.cloud.common.domain.flink.enums.EDiagnosisRule;
import com.oppo.cloud.common.domain.flink.enums.EDiagnosisTurningStatus;
import com.oppo.cloud.diagnosis.domain.diagnosis.DiagnosisContext;
import com.oppo.cloud.diagnosis.domain.diagnosis.RcJobDiagnosis;
import com.oppo.cloud.diagnosis.domain.diagnosis.RcJobDiagnosisAdvice;
import com.oppo.cloud.diagnosis.util.DoctorUtil;
import com.oppo.cloud.diagnosis.util.MonitorMetricUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.oppo.cloud.diagnosis.constant.MonitorMetricConstant.TM_AVG_CPU_USAGE_RATE;
import static com.oppo.cloud.diagnosis.constant.MonitorMetricConstant.TM_CPU_USAGE_RATE;


/**
 * 根据cpu和mem的最大值和均值判断是否需要缩容
 */
@Slf4j
@Component
public class AvgCpuLowRule extends BaseRule {
    @Autowired
    DoctorUtil doctorUtil;
    @Autowired
    TurningManager turningManager;
    @Autowired
    MonitorMetricUtil monitorMetricUtil;

    @Override
    public RcJobDiagnosisAdvice advice(DiagnosisContext r) {
        Double cpuLowThreshold = doctorUtil.getCpuLowThreshold(r);
        Double cpuLowTarget = doctorUtil.getCpuLowTarget(r);
        RcJobDiagnosisAdvice.RcJobDiagnosisAdviceBuilder builder = getBuilder(r);
        builder.adviceType(EDiagnosisRule.AvgCpuLowRule);
        // 尝试降低cpu
        // 判断单个cpu利用率的最大值低于阈值
        List<MetricResult.DataResult> cpuUsageList = r.getMetrics().get(TM_CPU_USAGE_RATE);
        if (cpuUsageList != null && cpuUsageList.size() > 0) {
            Double maxCpuUsage = cpuUsageList.stream()
                    .map(monitorMetricUtil::getFlatKeyValueStream)
                    .map(x -> monitorMetricUtil.getSmoothKeyValueStream(x, 3))
                    .map(monitorMetricUtil::getMaxOrNull)
                    .filter(Objects::nonNull)
                    .max(Double::compareTo)
                    .orElse(Double.MAX_VALUE);
            if (maxCpuUsage < cpuLowThreshold) {
                TurningAdvice turning = turningManager.turningCpuDown(r);
                if (turning != null && turning.getStatus().equals(EDiagnosisTurningStatus.HAS_ADVICE)) {
                    RcJobDiagnosisAdvice build = convertTurningToAdviceBuilder(turning,builder)
                            .hasAdvice(true)
                            .adviceDescription(String.format("作业的tm最大归一化cpu利用率低(%.2f%%)",
                                    maxCpuUsage * 100))
                            .build();
                    convertAdviceToRcJobDiagnosis(build,r);
                    String resourceChange = buildResourceChange(r);
                    String conclusion = String.format("作业TM CPU最大使用率%.2f%%,低于阈值%.2f%%,请扩充容量,使CPU使用率接近%.2f%%,%s",
                            maxCpuUsage*100,cpuLowThreshold*100,cpuLowTarget*100,resourceChange);
                    DiagnosisRuleReport diagnosisRuleReport = new DiagnosisRuleReport();
                    diagnosisRuleReport.setTitle("CPU利用率低分析");
                    diagnosisRuleReport.setConclusion(conclusion);
                    DiagnosisRuleLineChart diagnosisRuleLineChart = new DiagnosisRuleLineChart();
                    diagnosisRuleLineChart.setTitle("作业CPU使用率");
                    diagnosisRuleLineChart.setYAxisUnit("%");
                    diagnosisRuleLineChart.setYAxisValueType(EDiagnosisReportYAxisType.Percent.name());
                    diagnosisRuleLineChart.setYAxisMax(1d);
                    diagnosisRuleLineChart.setYAxisMin(0d);
                    DiagnosisRuleLine line = new DiagnosisRuleLine();
                    line.setLabel("Task CPU使用率");
                    line.setData(cpuUsageList);
                    diagnosisRuleLineChart.setLine(line);
                    Map<String,Double> constLine = new HashMap<>();
                    constLine.put("阈值",cpuLowThreshold);
                    diagnosisRuleLineChart.setConstLines(constLine);
                    diagnosisRuleReport.setIDiagnosisRuleCharts(Lists.newArrayList(diagnosisRuleLineChart));
                    build.setDiagnosisRuleReport(diagnosisRuleReport);
                    return build;
                } else {
                    String desc = String.format("作业的tm最大归一化cpu利用率低(%.2f%%)",
                            maxCpuUsage * 100);
                    if (turning != null) {
                        desc = desc + "," + turning.getDescription();
                    }
                    return builder
                            .hasAdvice(false)
                            .adviceDescription(desc)
                            .build();
                }
            } else {
                log.debug(String.format("%s最大tmcpu利用率%.2f", r.getRcJobDiagnosis().getJobName(),
                        maxCpuUsage));
            }
        }

        return builder.hasAdvice(false)
                .adviceDescription("无建议")
                .build();
    }


}
