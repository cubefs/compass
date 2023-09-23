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
import com.oppo.cloud.common.domain.flink.report.DiagnosisRuleLine;
import com.oppo.cloud.common.domain.flink.report.DiagnosisRuleLineChart;
import com.oppo.cloud.common.domain.flink.report.DiagnosisRuleReport;
import com.oppo.cloud.flink.advice.BaseRule;
import com.oppo.cloud.flink.constant.DiagnosisParamsConstants;
import com.oppo.cloud.common.domain.flink.enums.FlinkRule;
import com.oppo.cloud.flink.domain.diagnosis.DiagnosisContext;
import com.oppo.cloud.flink.domain.diagnosis.RcJobDiagnosisAdvice;
import com.oppo.cloud.common.domain.flink.enums.DiagnosisReportYAxisType;
import com.oppo.cloud.flink.util.MonitorMetricUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.oppo.cloud.flink.constant.MonitorMetricConstant.BACK_PRESSURE_VERTICES;


/**
 * 慢算子监测规则
 */
@Component
@Slf4j
public class BackPressureRule extends BaseRule {

    @Autowired
    private MonitorMetricUtil monitorMetricUtil;

    @Data
    @AllArgsConstructor
    public static class BackPressureRuleData extends RcJobDiagnosisAdvice {
        private List<BackPressureRecord> bpRecord;

        public BackPressureRuleData() {
            this.bpRecord = new ArrayList<>();
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BackPressureRecord {
        private String jobName;
        private String taskName;
        private String taskId;
        private String tmId;
        private String subtaskIndex;
    }

    @Autowired
    DiagnosisParamsConstants cons;

    @Override
    public RcJobDiagnosisAdvice advice(DiagnosisContext context) {
        BackPressureRuleData data = new BackPressureRuleData();
        buildAdvice(context, data);
        data.setAdviceType(FlinkRule.BackPressure);
        data.setHasAdvice(false);
        List<MetricResult.DataResult> dataResults = context.getMetrics().get(BACK_PRESSURE_VERTICES);
        if (dataResults == null) {
            data.setAdviceDescription("dataResults为空");
            return data;
        }
        long backpressureDuration = cons.slowVerticesInoutDiffHighDuration;
        dataResults.forEach(dr -> {
            try {
                int step = monitorMetricUtil.getStep(context.getStart(), context.getEnd());
                Supplier<Stream<Double>> stream = monitorMetricUtil.getValueStream(dr);
                long highCount = stream.get().filter(isBackPressure -> {
                    if (isBackPressure == null) {
                        return false;
                    }
                    if (isBackPressure > 0) {
                        return true;
                    }
                    return false;
                }).count();
                long highDuration = highCount * step;
                if (highDuration > backpressureDuration) {
                    BackPressureRecord backPressureRecord = new BackPressureRecord();
                    backPressureRecord.setJobName(context.getRcJobDiagnosis().getJobName());
                    backPressureRecord.setTmId((String) dr.getMetric().get("tm_id"));
                    backPressureRecord.setTaskName((String) dr.getMetric().get("task_name"));
                    if (data.getBpRecord().size() == 0) {
                        data.getBpRecord().add(backPressureRecord);
                    }
                }
            } catch (Throwable e) {
                log.error("计算反压报错:" + e.getMessage(), e);
            }
        });
        Set<String> tasks = new HashSet<>();
        data.getBpRecord().stream().forEach(x -> {
            tasks.add(x.getTaskName());
        });
        String distinctTaskName = tasks.toString();
        if (data.getBpRecord().size() != 0) {
            data.setHasAdvice(true);
            data.setAdviceDescription("存在反压:" + distinctTaskName);
            data.setSlowTasks(distinctTaskName);
            String conclusion = String.format("存在反压:%s,建议优化代码提高下游处理速度" , distinctTaskName);
            DiagnosisRuleReport diagnosisRuleReport = new DiagnosisRuleReport();
            diagnosisRuleReport.setTitle("反压算子分析");
            diagnosisRuleReport.setConclusion(conclusion);
            DiagnosisRuleLineChart diagnosisRuleLineChart = new DiagnosisRuleLineChart();
            diagnosisRuleLineChart.setTitle("反压");
            diagnosisRuleLineChart.setYAxisUnit("(0:无,1:有)");
            diagnosisRuleLineChart.setYAxisValueType(DiagnosisReportYAxisType.Numeric.name());
            diagnosisRuleLineChart.setYAxisMax(2d);
            diagnosisRuleLineChart.setYAxisMin(0d);
            DiagnosisRuleLine line = new DiagnosisRuleLine();
            line.setLabel("Task 反压");
            line.setData(dataResults);
            diagnosisRuleLineChart.setLine(line);
            Map<String, Double> constLine = new HashMap<>();
            diagnosisRuleLineChart.setConstLines(constLine);
            diagnosisRuleReport.setIDiagnosisRuleCharts(Lists.newArrayList(diagnosisRuleLineChart));
            data.setDiagnosisRuleReport(diagnosisRuleReport);
        } else {
            data.setAdviceDescription("无建议");
        }
        return data;
    }
}
