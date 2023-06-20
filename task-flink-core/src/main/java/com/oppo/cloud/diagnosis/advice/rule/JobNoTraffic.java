package com.oppo.cloud.diagnosis.advice.rule;

import com.google.common.collect.Lists;
import com.oppo.cloud.common.domain.flink.enums.DiagnosisReportYAxisType;
import com.oppo.cloud.common.domain.flink.metric.MetricResult;
import com.oppo.cloud.common.domain.flink.report.DiagnosisRuleLine;
import com.oppo.cloud.common.domain.flink.report.DiagnosisRuleLineChart;
import com.oppo.cloud.common.domain.flink.report.DiagnosisRuleReport;
import com.oppo.cloud.diagnosis.advice.BaseRule;
import com.oppo.cloud.common.domain.flink.enums.FlinkRule;
import com.oppo.cloud.diagnosis.domain.diagnosis.DiagnosisContext;
import com.oppo.cloud.diagnosis.domain.diagnosis.RcJobDiagnosisAdvice;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.oppo.cloud.common.domain.flink.enums.DiagnosisParam.FlowMax;
import static com.oppo.cloud.diagnosis.constant.MonitorMetricConstant.JOB_DATA_FLOW_RATE;


/**
 * 判断是否是无流量的任务
 */
@Component
public class JobNoTraffic extends BaseRule {
    @Override
    public RcJobDiagnosisAdvice advice(DiagnosisContext context) {
        RcJobDiagnosisAdvice.RcJobDiagnosisAdviceBuilder builder = getBuilder(context);
        builder.adviceType(FlinkRule.JobNoTraffic);
        // 没有流量建议下线,这里用等于0,不用null,因为null的任务不一定是读kafka，可能读其他数据源，这时不应该下线
        Object flowObj = context.getMessages().getOrDefault(FlowMax, -1);
        if (flowObj != null && (Integer) flowObj == 0) {
            builder
                    .hasAdvice(true)
                    .diagnosisParallel(1)
                    .diagnosisJmMem(1024)
                    .adviceDescription("该任务在诊断周期内没有流量，建议资源调到最小");
            String conclusion = String.format("该任务在诊断周期内没有流量，建议资源调到最小");
            DiagnosisRuleReport diagnosisRuleReport = new DiagnosisRuleReport();
            diagnosisRuleReport.setTitle("作业流量分析");
            diagnosisRuleReport.setConclusion(conclusion);
            DiagnosisRuleLineChart diagnosisRuleLineChart = new DiagnosisRuleLineChart();
            diagnosisRuleLineChart.setTitle("作业流量");
            diagnosisRuleLineChart.setYAxisUnit("(bytes/s)");
            diagnosisRuleLineChart.setYAxisValueType(DiagnosisReportYAxisType.BytesPerSecond.name());
            DiagnosisRuleLine line = new DiagnosisRuleLine();
            line.setLabel("作业流量");
            List<MetricResult.DataResult> jobFlowList = context.getMetrics().get(JOB_DATA_FLOW_RATE);
            line.setData(jobFlowList);
            diagnosisRuleLineChart.setLine(line);
            diagnosisRuleReport.setIDiagnosisRuleCharts(Lists.newArrayList(diagnosisRuleLineChart));
            builder.diagnosisRuleReport(diagnosisRuleReport);
            return builder
                    .build();
        }
        return builder
                .hasAdvice(false)
                .adviceDescription("无建议")
                .build();
    }
}
