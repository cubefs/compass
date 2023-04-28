package com.oppo.cloud.diagnosis.advice.rule;

import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Lists;
import com.oppo.cloud.common.domain.flink.enums.EDiagnosisReportYAxisType;
import com.oppo.cloud.common.domain.flink.enums.EDiagnosisRule;
import com.oppo.cloud.common.domain.flink.enums.EDiagnosisTurningStatus;
import com.oppo.cloud.common.domain.flink.metric.MetricResult;
import com.oppo.cloud.common.domain.flink.report.DiagnosisRuleLine;
import com.oppo.cloud.common.domain.flink.report.DiagnosisRuleLineChart;
import com.oppo.cloud.common.domain.flink.report.DiagnosisRuleReport;
import com.oppo.cloud.diagnosis.advice.BaseRule;
import com.oppo.cloud.diagnosis.advice.turning.TurningAdvice;
import com.oppo.cloud.diagnosis.advice.turning.TurningManager;
import com.oppo.cloud.diagnosis.domain.diagnosis.DiagnosisContext;
import com.oppo.cloud.diagnosis.domain.diagnosis.RcJobDiagnosis;
import com.oppo.cloud.diagnosis.domain.diagnosis.RcJobDiagnosisAdvice;
import com.oppo.cloud.diagnosis.util.DoctorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.oppo.cloud.diagnosis.constant.MonitorMetricConstant.TM_AVG_CPU_USAGE_RATE;
import static com.oppo.cloud.diagnosis.constant.MonitorMetricConstant.TM_CPU_USAGE_RATE;

/**
 * 根据cpu和mem使用的均值判断是否需要扩容
 */
@Component
public class AvgCpuHighRule extends BaseRule {
    @Autowired
    DoctorUtil doctorUtil;
    @Autowired
    TurningManager turningManager ;

    @Override
    public RcJobDiagnosisAdvice advice(DiagnosisContext r) {
        Double cpuHighThreshold = doctorUtil.getCpuHighThreshold(r);
        Double cpuHighTarget = doctorUtil.getCpuHighTarget(r);
        RcJobDiagnosis rcJobDiagnosis = r.getRcJobDiagnosis();
        RcJobDiagnosisAdvice.RcJobDiagnosisAdviceBuilder builder = getBuilder(r);
        builder.adviceType(EDiagnosisRule.AvgCpuHighRule);
        // 判断平均cpu是否高
        if (rcJobDiagnosis.getTmAvgCpuUsageAvg() != null
                && notNullGt(rcJobDiagnosis.getTmAvgCpuUsageAvg(), cpuHighThreshold.floatValue())) {
            TurningAdvice turning = turningManager.turningCpuUp(r);
            if (turning != null && turning.getStatus().equals(EDiagnosisTurningStatus.HAS_ADVICE)) {

                RcJobDiagnosisAdvice build = convertTurningToAdviceBuilder(turning,builder)
                        .hasAdvice(true)
                        .adviceDescription(String.format("归一化cpu平均利用率高(%.2f%%)",
                                rcJobDiagnosis.getTmAvgCpuUsageAvg() * 100)
                        ).build();
                convertAdviceToRcJobDiagnosis(build,r);
                String resourceChange = buildResourceChange(r);
                String conclusion = String.format("作业CPU均值使用率%.2f%%,超过阈值%.2f%%,请扩充容量,使CPU使用率接近%.2f%%,%s",
                        rcJobDiagnosis.getTmAvgCpuUsageAvg()*100,cpuHighThreshold*100,cpuHighTarget*100,resourceChange);
                DiagnosisRuleReport diagnosisRuleReport = new DiagnosisRuleReport();
                diagnosisRuleReport.setTitle("CPU均值利用率高分析");
                diagnosisRuleReport.setConclusion(conclusion);
                DiagnosisRuleLineChart diagnosisRuleLineChart = new DiagnosisRuleLineChart();
                diagnosisRuleLineChart.setTitle("作业CPU使用率");
                diagnosisRuleLineChart.setYAxisUnit("%");
                diagnosisRuleLineChart.setYAxisValueType(EDiagnosisReportYAxisType.Percent.name());
                diagnosisRuleLineChart.setYAxisMax(1d);
                diagnosisRuleLineChart.setYAxisMin(0d);
                DiagnosisRuleLine line = new DiagnosisRuleLine();
                line.setLabel("Task CPU使用率");
                List<MetricResult.DataResult> cpuUsageList = r.getMetrics().get(TM_AVG_CPU_USAGE_RATE);
                line.setData(cpuUsageList);
                diagnosisRuleLineChart.setLine(line);
                Map<String,Double> constLine = new HashMap<>();
                constLine.put("均值",rcJobDiagnosis.getTmAvgCpuUsageAvg().doubleValue());
                constLine.put("阈值",cpuHighThreshold);
                diagnosisRuleLineChart.setConstLines(constLine);
                diagnosisRuleReport.setIDiagnosisRuleCharts(Lists.newArrayList(diagnosisRuleLineChart));
                build.setDiagnosisRuleReport(diagnosisRuleReport);
                return build;
            } else {
                String desc = String.format(
                        "归一化cpu平均利用率%.2f%%高于%.2f%%",
                        rcJobDiagnosis.getTmAvgCpuUsageAvg() * 100,
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

        return builder
                .hasAdvice(false)
                .adviceDescription("无建议")
                .build();
    }

}
