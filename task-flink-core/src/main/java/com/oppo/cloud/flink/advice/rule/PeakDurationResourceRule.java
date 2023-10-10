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

import com.alibaba.fastjson2.JSON;
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
import com.oppo.cloud.common.domain.flink.enums.DiagnosisParam;
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
import java.util.OptionalDouble;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.oppo.cloud.flink.constant.MonitorMetricConstant.TM_CPU_USAGE_RATE;


/**
 * Monitor the resource situation during peak times and determine whether scaling is necessary.
 */
@Slf4j
@Component
public class PeakDurationResourceRule extends BaseRule {
    @Autowired
    DiagnosisParamsConstants cons;

    @Autowired
    DoctorUtil doctorUtil;

    @Autowired
    TurningManager turningManager;

    @Autowired
    MonitorMetricUtil monitorMetricUtil;

    @Override
    public RcJobDiagnosisAdvice advice(DiagnosisContext context) {
        Double cpuHighThreshold = doctorUtil.getCpuHighThreshold(context);
        Double cpuHighTarget = doctorUtil.getCpuHighTarget(context);
        RcJobDiagnosisAdvice.RcJobDiagnosisAdviceBuilder builder = getBuilder(context);
        builder.adviceType(FlinkRule.PeekDurationResourceRule);
        // Determine if the CPU peak utilization is high.
        // Obtain the CPU utilization rate for each tm.
        List<MetricResult.DataResult> cpuUsageList = context.getMetrics().get(TM_CPU_USAGE_RATE);
        if (cpuUsageList == null || cpuUsageList.size() == 0) {
            return builder
                    .hasAdvice(false)
                    .adviceDescription("Tm cpu usage is empty")
                    .build();
        }
        for (MetricResult.DataResult dataResult : cpuUsageList) {
            try {
                // If the CPU is above the threshold for the past five minutes, take the average value instead of
                // the cumulative time. Smooth the data to eliminate spikes, but do not use smoothing to reduce the
                // number of data points.
                long end = context.getEnd();
                long minutes5Before = end - cons.getTmCpuHighLatestNMinutes() * 60;
                Supplier<Stream<MetricResult.KeyValue>> flatMetric = monitorMetricUtil.getFlatKeyValueStream(dataResult);
                OptionalDouble averageUnitCpuMinutes5Before = flatMetric.get()
                        .filter(x -> {
                                    if (x == null) {
                                        return false;
                                    }
                                    if (x.getTs() == null) {
                                        return false;
                                    }
                                    return x.getTs() >= minutes5Before;
                                }
                        )
                        .map(MetricResult.KeyValue::getValue)
                        .mapToDouble(x -> x)
                        .average();
                if (averageUnitCpuMinutes5Before.isPresent()
                        && averageUnitCpuMinutes5Before.getAsDouble() > cpuHighThreshold) {
                    log.debug("Tm's CPU average value has been above the threshold for the past {} minutes: {}",
                            cons.getTmCpuHighLatestNMinutes(),
                            context.getRcJobDiagnosis().getJobName() + ":" + JSON.toJSONString(dataResult));
                    log.info("CPU average utilization rate of tm in the job section for the past {} minutes: {}% exceeds {}%.",
                            cons.getTmCpuHighLatestNMinutes(), averageUnitCpuMinutes5Before.getAsDouble() * 100,
                            cpuHighThreshold.floatValue() * 100);
                    Double changeRate = averageUnitCpuMinutes5Before.getAsDouble() / cpuHighTarget - 1;
                    context.getMessages().put(DiagnosisParam.GrowCpuChangeRate, changeRate);
                    TurningAdvice turning = turningManager.turningCpuUp(context);
                    if (turning != null && turning.getStatus().equals(DiagnosisTurningStatus.HAS_ADVICE)) {
                        RcJobDiagnosisAdvice build = convertTurningToAdviceBuilder(turning, builder)
                                .hasAdvice(true)
                                .adviceDescription(String.format("作业部分TM最近%d分钟CPU均值利用率:%.2f%%,超过%.2f%%",
                                        cons.getTmCpuHighLatestNMinutes(), averageUnitCpuMinutes5Before.getAsDouble() * 100,
                                        cpuHighThreshold.floatValue() * 100)
                                ).build();
                        convertAdviceToRcJobDiagnosis(build, context);
                        String resourceChange = buildResourceChange(context);
                        String conclusion = String.format("作业部分TM最近%d分钟CPU均值利用率:%.2f%%,超过%.2f%%,%s",
                                cons.getTmCpuHighLatestNMinutes(), averageUnitCpuMinutes5Before.getAsDouble() * 100,
                                cpuHighThreshold.floatValue() * 100, resourceChange);
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
                        line.setData(cpuUsageList);
                        diagnosisRuleLineChart.setLine(line);
                        Map<String, Double> constLine = new HashMap<>();
                        constLine.put("阈值", cpuHighThreshold);
                        constLine.put("最近五分钟CPU均值", averageUnitCpuMinutes5Before.getAsDouble());
                        diagnosisRuleLineChart.setConstLines(constLine);
                        diagnosisRuleReport.setIDiagnosisRuleCharts(Lists.newArrayList(diagnosisRuleLineChart));
                        build.setDiagnosisRuleReport(diagnosisRuleReport);
                        return build;
                    } else {
                        String desc = String.format("作业部分tm 最近%d 分钟 cpu均值利用率:%.2f%% 超过%.2f%%",
                                cons.getTmCpuHighLatestNMinutes(), averageUnitCpuMinutes5Before.getAsDouble() * 100,
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

                // Total cumulative time is above the threshold.
                int step = monitorMetricUtil.getStep(context.getStart(), context.getEnd());
                Supplier<Stream<MetricResult.KeyValue>> smoothMetric = monitorMetricUtil.getSmoothKeyValueStream(
                        monitorMetricUtil.getFlatKeyValueStream(dataResult), 3);
                List<Double> collectHighValue = smoothMetric.get()
                        .map(MetricResult.KeyValue::getValue)
                        .filter(cpuUsage -> {
                            if (cpuUsage == null) {
                                return false;
                            }
                            if (cpuUsage > cpuHighThreshold.floatValue()) {
                                return true;
                            }
                            return false;
                        }).collect(Collectors.toList());
                long highDuration = (long) collectHighValue.size() * step;
                if (highDuration > cons.getTmPeakHighTimeThreshold()) {
                    log.debug("Data with high individual peak utilization rate for tm:" + context.getRcJobDiagnosis().getJobName() + ":" + JSON.toJSONString(dataResult));
                    TurningAdvice turning = turningManager.turningCpuUp(context);
                    if (turning != null && turning.getStatus().equals(DiagnosisTurningStatus.HAS_ADVICE)) {
                        RcJobDiagnosisAdvice build = convertTurningToAdviceBuilder(turning, builder)
                                .hasAdvice(true)
                                .adviceDescription(String.format("作业部分tm峰值归一化cpu利用率超过%.2f%%的时间累计超过%.2f秒",
                                        cpuHighThreshold.floatValue() * 100, cons.getTmPeakHighTimeThreshold())
                                ).build();
                        convertAdviceToRcJobDiagnosis(build, context);
                        String resourceChange = buildResourceChange(context);
                        String conclusion = String.format("作业部分tm峰值归一化cpu利用率超过%.2f%%的时间累计超过%.2f秒,%s",
                                cpuHighThreshold.floatValue() * 100, cons.getTmPeakHighTimeThreshold(), resourceChange);
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
                        line.setData(cpuUsageList);
                        diagnosisRuleLineChart.setLine(line);
                        Map<String, Double> constLine = new HashMap<>();
                        constLine.put("阈值", cpuHighThreshold);
                        diagnosisRuleLineChart.setConstLines(constLine);
                        diagnosisRuleReport.setIDiagnosisRuleCharts(Lists.newArrayList(diagnosisRuleLineChart));
                        build.setDiagnosisRuleReport(diagnosisRuleReport);
                        return build;
                    } else {
                        String desc = String.format("作业部分tm峰值cpu利用率高于%.2f%%", cpuHighThreshold.floatValue() * 100);
                        if (turning != null) {
                            desc = desc + "," + turning.getDescription();
                        }
                        return builder
                                .hasAdvice(false)
                                .adviceDescription(desc)
                                .build();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Error in tuning when CPU peak utilization rate is high: " + e.getMessage());
            }
        }

        return builder
                .hasAdvice(false)
                .adviceDescription("No advice")
                .build();
    }

}
