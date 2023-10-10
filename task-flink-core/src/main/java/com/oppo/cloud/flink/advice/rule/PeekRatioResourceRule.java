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
import com.oppo.cloud.flink.constant.DiagnosisParamsConstants;
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
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.oppo.cloud.flink.constant.MonitorMetricConstant.TM_CPU_USAGE_RATE;


/**
 * Monitor the resource situation during peak times and determine whether scaling is necessary.
 */
@Slf4j
@Component
public class PeekRatioResourceRule extends BaseRule {
    @Autowired
    DiagnosisParamsConstants cons;

    @Autowired
    DoctorUtil doctorUtil;

    @Autowired
    TurningManager turningManager;

    @Autowired
    MonitorMetricUtil monitorMetricUtil;

    @Override
    public RcJobDiagnosisAdvice advice(DiagnosisContext r) {
        final Double cpuHighThreshold = doctorUtil.getCpuHighThreshold(r);
        RcJobDiagnosisAdvice.RcJobDiagnosisAdviceBuilder builder = getBuilder(r);
        builder.adviceType(FlinkRule.PeekDurationResourceRule);
        List<MetricResult.DataResult> dataResults = r.getMetrics().get(TM_CPU_USAGE_RATE);
        if (dataResults == null || dataResults.size() == 0) {
            return builder
                    .hasAdvice(false)
                    .adviceDescription("The CPU average utilization rate indicator is empty.")
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
                    log.debug("Data with high average peak utilization rate for tm:" + stream);
                    TurningAdvice turning = turningManager.turningCpuUp(r);
                    if (turning != null && turning.getStatus().equals(DiagnosisTurningStatus.HAS_ADVICE)) {
                        RcJobDiagnosisAdvice build = convertTurningToAdviceBuilder(turning, builder)
                                .hasAdvice(true)
                                .adviceDescription(String.format("作业部分TM峰值归一化cpu超过%.2f%%的时间累计超过%.2f%%",
                                        cpuHighThreshold * 100, cons.getCpuUsageAccHighTimeRate() * 100))
                                .build();
                        convertAdviceToRcJobDiagnosis(build, r);
                        String resourceChange = buildResourceChange(r);
                        String conclusion = String.format("作业部分TM峰值归一化cpu超过%.2f%%的时间累计超过%.2f%%,%s",
                                cpuHighThreshold * 100, cons.getCpuUsageAccHighTimeRate() * 100, resourceChange);
                        DiagnosisRuleReport diagnosisRuleReport = new DiagnosisRuleReport();
                        diagnosisRuleReport.setTitle("峰值CPU利用率高分析");
                        diagnosisRuleReport.setConclusion(conclusion);
                        DiagnosisRuleLineChart diagnosisRuleLineChart = new DiagnosisRuleLineChart();
                        diagnosisRuleLineChart.setTitle("作业CPU使用率");
                        diagnosisRuleLineChart.setYAxisUnit("%");
                        diagnosisRuleLineChart.setYAxisValueType(DiagnosisReportYAxisType.Percent.name());
                        diagnosisRuleLineChart.setYAxisMax(1d);
                        diagnosisRuleLineChart.setYAxisMin(0d);
                        DiagnosisRuleLine line = new DiagnosisRuleLine();
                        line.setLabel("作业CPU使用率");
                        line.setData(dataResults);
                        diagnosisRuleLineChart.setLine(line);
                        Map<String, Double> constLine = new HashMap<>();
                        constLine.put("阈值", cpuHighThreshold);
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
                .adviceDescription("No advice")
                .build();
    }
}
