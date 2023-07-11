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
import com.oppo.cloud.common.domain.flink.metric.MetricResult;
import com.oppo.cloud.common.domain.flink.report.DiagnosisRuleBarChart;
import com.oppo.cloud.common.domain.flink.report.DiagnosisRulePoint;
import com.oppo.cloud.common.domain.flink.report.DiagnosisRuleReport;
import com.oppo.cloud.flink.advice.BaseRule;
import com.oppo.cloud.flink.constant.DiagnosisParamsConstants;
import com.oppo.cloud.common.domain.flink.enums.FlinkRule;
import com.oppo.cloud.flink.domain.diagnosis.DiagnosisContext;
import com.oppo.cloud.flink.domain.diagnosis.RcJobDiagnosis;
import com.oppo.cloud.flink.domain.diagnosis.RcJobDiagnosisAdvice;
import com.oppo.cloud.flink.util.MonitorMetricUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.oppo.cloud.flink.constant.MonitorMetricConstant.TM_MANAGE_MEM_TOTAL;
import static com.oppo.cloud.flink.constant.MonitorMetricConstant.TM_MANAGE_MEM_USAGE;


/**
 * 根据tm manage 内存使用率给出参数建议
 * <p>
 * manage 内存最小值
 * taskmanager.memory.managed.size:100m
 * taskmanager.memory.managed.fraction:0.01
 */
@Component
@Slf4j
public class TmManagedMemory extends BaseRule {
    @Autowired
    DiagnosisParamsConstants cons;
    private MonitorMetricUtil monitorMetricUtil = new MonitorMetricUtil();

    @Override
    public RcJobDiagnosisAdvice advice(DiagnosisContext context) {
        RcJobDiagnosisAdvice data = new RcJobDiagnosisAdvice();
        buildAdvice(context, data);
        data.setAdviceType(FlinkRule.TmManagedMemory);
        data.setHasAdvice(false);
        RcJobDiagnosis rcJobDiagnosis = context.getRcJobDiagnosis();
        List<MetricResult.DataResult> manageUsageList = context.getMetrics().get(TM_MANAGE_MEM_USAGE);
        if (manageUsageList == null || manageUsageList.size() == 0) {
            data.setAdviceDescription(String.format("%s manageUsageList 为空", context.getRcJobDiagnosis().getJobName()));
            return data;
        }
        Optional<Double> maxUsageOption = manageUsageList
                .stream()
                .map(monitorMetricUtil::getMaxOrNull)
                .filter(Objects::nonNull).max(Double::compareTo);
        Double maxUsage = maxUsageOption.orElse(null);
        if (maxUsage == null) {
            data.setAdviceDescription("maxUsage 为空");
            return data;
        }
        List<MetricResult.DataResult> manageTotalList = context.getMetrics().get(TM_MANAGE_MEM_TOTAL);
        if (manageTotalList == null) {
            log.debug(String.format("%s manageTotalList为空", context.getRcJobDiagnosis().getJobName()));
            data.setAdviceDescription(String.format("%s manageTotalList为空", context.getRcJobDiagnosis().getJobName()));
            return data;
        }
        Optional<Double> maxTotalOption = manageTotalList
                .stream()
                .map(monitorMetricUtil::getMaxOrNull)
                .filter(Objects::nonNull).max(Double::compareTo);
        Double maxTotal = maxTotalOption.orElse(null);
        if (maxTotal == null) {
            data.setAdviceDescription("maxTotal 为空");
            return data;
        }
        double maxTotalMb = maxTotal / 1024 / 1024;
        StringBuilder descriptionBuilder = new StringBuilder();
        double needManageMemory = maxUsage / cons.tmManageMemUsageCutThreshold;
        long needManageMemoryMb = (long) needManageMemory / 1024 / 1024;
        if (needManageMemoryMb < 100) {
            needManageMemoryMb = 100;
        }
        // 观察到有设置100MB管理内存，但是实际出来有105M，导致重复设置
        if (needManageMemoryMb + 100 < maxTotalMb) {
            descriptionBuilder
                    .append(String.format("建议设置taskmanager.memory.managed.size为%dm", needManageMemoryMb));
            data.setHasAdvice(true);
            data.setDiagnosisManageMem((int) needManageMemoryMb);
            data.setAdviceDescription(descriptionBuilder.toString());
            String conclusion = String.format("当前TM总Manage内存%.0f MB,已使用Manage内存%.0f MB,建议设置TM Manage内存为%d MB"
                    , maxTotalMb, maxUsage / 1024 / 1024, needManageMemoryMb);
            DiagnosisRuleReport diagnosisRuleReport = new DiagnosisRuleReport();
            diagnosisRuleReport.setTitle("Manage内存分析");
            diagnosisRuleReport.setConclusion(conclusion);
            DiagnosisRuleBarChart diagnosisRuleBarChart = new DiagnosisRuleBarChart();
            diagnosisRuleBarChart.setTitle("Manage内存");
            diagnosisRuleBarChart.setYAxisUnit("(MB)");
            DiagnosisRulePoint point1 = new DiagnosisRulePoint();
            point1.setKey("总Manage内存数");
            point1.setValue(maxTotalMb);
            DiagnosisRulePoint point2 = new DiagnosisRulePoint();
            point2.setKey("已使用Manage内存数");
            point2.setValue(maxUsage / 1024 / 1024);
            diagnosisRuleBarChart.setBars(Lists.newArrayList(point1, point2));
            diagnosisRuleReport.setIDiagnosisRuleCharts(Lists.newArrayList(diagnosisRuleBarChart));
            data.setDiagnosisRuleReport(diagnosisRuleReport);
            return data;
        }
        data.setAdviceDescription("无建议");
        return data;
    }
}
