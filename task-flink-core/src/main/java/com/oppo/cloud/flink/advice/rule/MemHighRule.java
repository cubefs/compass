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
import com.oppo.cloud.flink.domain.diagnosis.RcJobDiagnosis;
import com.oppo.cloud.flink.domain.diagnosis.RcJobDiagnosisAdvice;
import com.oppo.cloud.common.domain.flink.metric.MetricResult;
import com.oppo.cloud.flink.util.DoctorUtil;
import com.oppo.cloud.flink.util.MonitorMetricUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.oppo.cloud.flink.constant.MonitorMetricConstant.TM_HEAP_MEM_USAGE_RATE;


/**
 * Expanding memory rules.
 */
@Component
@Slf4j
public class MemHighRule extends BaseRule {

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
        Double memHighThreshold = doctorUtil.getMemHighThreshold(r);
        RcJobDiagnosis rcJobDiagnosis = r.getRcJobDiagnosis();
        RcJobDiagnosisAdvice.RcJobDiagnosisAdviceBuilder builder = getBuilder(r);
        builder.adviceType(FlinkRule.TmMemoryHigh);
        // Try to reduce the tm memory, and determine if the largest heap memory utilization rate is less than the threshold.
        List<MetricResult.DataResult> memUsageList = r.getMetrics().get(TM_HEAP_MEM_USAGE_RATE);
        if (memUsageList != null && memUsageList.size() > 0) {
            Double maxMemUsage = memUsageList.stream()
                    .map(monitorMetricUtil::getAvg).max(Double::compareTo)
                    .orElse(Double.MAX_VALUE);
            if (maxMemUsage > memHighThreshold) {
                TurningAdvice turning = turningManager.turningMemUp(r);
                String description = String.format("作业最大堆内存利用率(%.2f%%) 高于阈值%.2f%%;",
                        maxMemUsage * 100, memHighThreshold * 100);
                if (turning != null && turning.getStatus().equals(DiagnosisTurningStatus.HAS_ADVICE)) {
                    RcJobDiagnosisAdvice build = builder
                            .hasAdvice(true)
                            .diagnosisParallel(turning.getParallel())
                            .diagnosisTmMem(turning.getTmMem())
                            .diagnosisTmSlotNum(turning.getTmSlotNum())
                            .diagnosisTmCore(turning.getVcore())
                            .diagnosisTmNum(turning.getTmNum())
                            .adviceDescription(description)
                            .build();
                    convertAdviceToRcJobDiagnosis(build, r);
                    String resourceChange = buildResourceChange(r);
                    String conclusion = String.format("作业最大堆内存利用率(%.2f%%) 高于阈值%.2f%%;%s",
                            maxMemUsage * 100, memHighThreshold * 100, resourceChange);
                    DiagnosisRuleReport diagnosisRuleReport = new DiagnosisRuleReport();
                    diagnosisRuleReport.setTitle("内存利用率高分析");
                    diagnosisRuleReport.setConclusion(conclusion);
                    DiagnosisRuleLineChart diagnosisRuleLineChart = new DiagnosisRuleLineChart();
                    diagnosisRuleLineChart.setTitle("作业内存使用率");
                    diagnosisRuleLineChart.setYAxisUnit("%");
                    diagnosisRuleLineChart.setYAxisValueType(DiagnosisReportYAxisType.Percent.name());
                    diagnosisRuleLineChart.setYAxisMax(1d);
                    diagnosisRuleLineChart.setYAxisMin(0d);
                    DiagnosisRuleLine line = new DiagnosisRuleLine();
                    line.setLabel("作业内存使用率");
                    line.setData(memUsageList);
                    diagnosisRuleLineChart.setLine(line);
                    Map<String, Double> constLine = new HashMap<>();
                    constLine.put("阈值", memHighThreshold);
                    constLine.put("内存利用率", maxMemUsage);
                    diagnosisRuleLineChart.setConstLines(constLine);
                    diagnosisRuleReport.setIDiagnosisRuleCharts(Lists.newArrayList(diagnosisRuleLineChart));
                    build.setDiagnosisRuleReport(diagnosisRuleReport);
                    return build;
                } else {
                    String desc = description;
                    if (turning != null) {
                        desc = desc + "," + turning.getDescription();
                    }
                    return builder
                            .adviceDescription(desc)
                            .build();
                }
            } else {
                log.debug(String.format("%s job's maximum tm heap memory utilization rate:%.2f", r.getRcJobDiagnosis().getJobName(), maxMemUsage));
            }
        }
        return builder
                .adviceDescription("No advice")
                .build();
    }
}
