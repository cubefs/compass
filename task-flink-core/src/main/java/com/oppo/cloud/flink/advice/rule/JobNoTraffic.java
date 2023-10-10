/*
 * Copyright 2023 OPPO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.oppo.cloud.flink.advice.rule;

import com.google.common.collect.Lists;
import com.oppo.cloud.common.domain.flink.enums.DiagnosisReportYAxisType;
import com.oppo.cloud.common.domain.flink.metric.MetricResult;
import com.oppo.cloud.common.domain.flink.report.DiagnosisRuleLine;
import com.oppo.cloud.common.domain.flink.report.DiagnosisRuleLineChart;
import com.oppo.cloud.common.domain.flink.report.DiagnosisRuleReport;
import com.oppo.cloud.flink.advice.BaseRule;
import com.oppo.cloud.common.domain.flink.enums.FlinkRule;
import com.oppo.cloud.flink.domain.diagnosis.DiagnosisContext;
import com.oppo.cloud.flink.domain.diagnosis.RcJobDiagnosisAdvice;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.oppo.cloud.common.domain.flink.enums.DiagnosisParam.FlowMax;
import static com.oppo.cloud.flink.constant.MonitorMetricConstant.JOB_DATA_FLOW_RATE;


/**
 * Determine whether it is a task without internet traffic.
 */
@Component
public class JobNoTraffic extends BaseRule {
    @Override
    public RcJobDiagnosisAdvice advice(DiagnosisContext context) {
        RcJobDiagnosisAdvice.RcJobDiagnosisAdviceBuilder builder = getBuilder(context);
        builder.adviceType(FlinkRule.JobNoTraffic);
        // If there is no traffic, it is recommended to go offline, using 0 instead of null.
        // This is because a null task may not be reading from Kafka, but from another data source,
        // and should not be taken offline.
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
                .adviceDescription("No advice")
                .build();
    }
}
