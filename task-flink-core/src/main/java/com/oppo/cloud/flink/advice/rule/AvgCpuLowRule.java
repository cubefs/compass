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
import com.oppo.cloud.flink.advice.turning.TurningAdvice;
import com.oppo.cloud.flink.advice.turning.TurningManager;
import com.oppo.cloud.common.domain.flink.enums.FlinkRule;
import com.oppo.cloud.common.domain.flink.enums.DiagnosisTurningStatus;
import com.oppo.cloud.flink.domain.diagnosis.DiagnosisContext;
import com.oppo.cloud.flink.domain.diagnosis.RcJobDiagnosisAdvice;
import com.oppo.cloud.flink.util.DoctorUtil;
import com.oppo.cloud.flink.util.MonitorMetricUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.oppo.cloud.flink.constant.MonitorMetricConstant.TM_CPU_USAGE_RATE;


/**
 * Based on the maximum and average values of CPU and memory usage to determine whether to scale down.
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
        builder.adviceType(FlinkRule.AvgCpuLowRule);
        // Try to reduce CPU usage.
        // To determine if the maximum utilization rate of a single CPU is below a threshold.
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
                if (turning != null && turning.getStatus().equals(DiagnosisTurningStatus.HAS_ADVICE)) {
                    RcJobDiagnosisAdvice build = convertTurningToAdviceBuilder(turning, builder)
                            .hasAdvice(true)
                            .adviceDescription(String.format("作业的tm最大归一化cpu利用率低(%.2f%%)",
                                    maxCpuUsage * 100))
                            .build();
                    convertAdviceToRcJobDiagnosis(build, r);
                    String resourceChange = buildResourceChange(r);
                    String conclusion = String.format("作业TM CPU最大使用率%.2f%%,低于阈值%.2f%%,请缩减容量,使CPU使用率接近%.2f%%,%s",
                            maxCpuUsage * 100, cpuLowThreshold * 100, cpuLowTarget * 100, resourceChange);
                    DiagnosisRuleReport diagnosisRuleReport = new DiagnosisRuleReport();
                    diagnosisRuleReport.setTitle("CPU利用率低分析");
                    diagnosisRuleReport.setConclusion(conclusion);
                    DiagnosisRuleLineChart diagnosisRuleLineChart = new DiagnosisRuleLineChart();
                    diagnosisRuleLineChart.setTitle("作业CPU使用率");
                    diagnosisRuleLineChart.setYAxisUnit("%");
                    diagnosisRuleLineChart.setYAxisValueType(DiagnosisReportYAxisType.Percent.name());
                    diagnosisRuleLineChart.setYAxisMax(1d);
                    diagnosisRuleLineChart.setYAxisMin(0d);
                    DiagnosisRuleLine line = new DiagnosisRuleLine();
                    line.setLabel("Task CPU使用率");
                    line.setData(cpuUsageList);
                    diagnosisRuleLineChart.setLine(line);
                    Map<String, Double> constLine = new HashMap<>();
                    constLine.put("阈值", cpuLowThreshold);
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
                .adviceDescription("No advice")
                .build();
    }


}
