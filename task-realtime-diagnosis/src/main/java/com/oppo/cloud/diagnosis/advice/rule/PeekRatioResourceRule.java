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
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.oppo.cloud.diagnosis.constant.MonitorMetricConstant.TM_CPU_USAGE_RATE;


/**
 * 监测峰值时候的资源情况，判断是否需要扩容
 */
@Slf4j
@Component
public class PeekRatioResourceRule extends BaseRule {
    @Autowired
    DiagnosisParamsConstants cons;
    @Autowired
    DoctorUtil doctorUtil ;
    @Autowired
    TurningManager turningManager ;
    @Autowired
    MonitorMetricUtil monitorMetricUtil;

    @Override
    public RcJobDiagnosisAdvice advice(DiagnosisContext r) {
        final Double cpuHighThreshold = doctorUtil.getCpuHighThreshold(r);
        RcJobDiagnosisAdvice.RcJobDiagnosisAdviceBuilder builder = getBuilder(r);
        builder.adviceType(EDiagnosisRule.PeekDurationResourceRule);
        List<MetricResult.DataResult> dataResults = r.getMetrics().get(TM_CPU_USAGE_RATE);
        if (dataResults == null || dataResults.size() == 0) {
            return builder
                    .hasAdvice(false)
                    .adviceDescription("cpu平均使用率指标为空")
                    .build();
        }
        try {
            for (MetricResult.DataResult dataResult : dataResults) {
                Supplier<Stream<MetricResult.KeyValue>> stream = monitorMetricUtil.getSmoothKeyValueStream(
                        monitorMetricUtil.getFlatKeyValueStream(dataResult), 3);
                long total = stream.get().count();
                long highCount = stream.get().map(MetricResult.KeyValue::getValue).filter(cpuUsage -> {
                    if (cpuUsage == null) {
                        return false;
                    }
                    if (cpuUsage > cpuHighThreshold) {
                        return true;
                    }
                    return false;
                }).count();
                double highRatio = (double) highCount / total;
                if (highRatio > cons.getCpuUsageAccHighTimeRate()) {
                    log.debug("tm平均峰值利用率高的数据:" + stream);
                    TurningAdvice turning = turningManager.turningCpuUp(r);
                    if (turning != null && turning.getStatus().equals(EDiagnosisTurningStatus.HAS_ADVICE)) {
                        RcJobDiagnosisAdvice build = convertTurningToAdviceBuilder(turning,builder)
                                .hasAdvice(true)
                                .adviceDescription(String.format("作业部分TM峰值归一化cpu超过%.2f%%的时间累计超过%.2f%%",
                                        cpuHighThreshold * 100, cons.getCpuUsageAccHighTimeRate() * 100))
                                .build();
                        convertAdviceToRcJobDiagnosis(build, r);
                        String resourceChange = buildResourceChange(r);
                        String conclusion = String.format("作业部分TM峰值归一化cpu超过%.2f%%的时间累计超过%.2f%%,%s",
                                cpuHighThreshold * 100, cons.getCpuUsageAccHighTimeRate() * 100,resourceChange);
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
                        line.setData(dataResults);
                        diagnosisRuleLineChart.setLine(line);
                        Map<String,Double> constLine = new HashMap<>();
                        constLine.put("阈值",cpuHighThreshold);
                        diagnosisRuleLineChart.setConstLines(constLine);
                        diagnosisRuleReport.setIDiagnosisRuleCharts(Lists.newArrayList(diagnosisRuleLineChart));
                        build.setDiagnosisRuleReport(diagnosisRuleReport);
                        return build;
                    } else {
                        String desc = String.format("作业部分tm峰值归一化cpu超过%.2f%%的时间累计超过%.2f%%,没有合适cpu方案",
                                cpuHighThreshold * 100, cons.getCpuUsageAccHighTimeRate() * 100);
                        if (turning != null) {
                            desc = desc + "," + turning.getDescription();
                        }
                        return builder
                                .hasAdvice(false)
                                .adviceDescription(desc)
                                .build();
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return builder
                .hasAdvice(false)
                .adviceDescription("无建议")
                .build();
    }
}
