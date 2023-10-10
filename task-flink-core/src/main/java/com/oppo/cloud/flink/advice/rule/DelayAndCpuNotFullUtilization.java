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
import com.oppo.cloud.flink.advice.turning.MemTurningByUsage;
import com.oppo.cloud.flink.advice.turning.TurningAdvice;
import com.oppo.cloud.flink.constant.DiagnosisParamsConstants;
import com.oppo.cloud.common.domain.flink.enums.FlinkRule;
import com.oppo.cloud.flink.domain.diagnosis.DiagnosisContext;
import com.oppo.cloud.flink.domain.diagnosis.RcJobDiagnosis;
import com.oppo.cloud.flink.domain.diagnosis.RcJobDiagnosisAdvice;
import com.oppo.cloud.flink.service.impl.FlinkDiagnosisMetricsServiceImpl;
import com.oppo.cloud.common.domain.flink.metric.MetricResult;
import com.oppo.cloud.flink.util.DoctorUtil;
import com.oppo.cloud.flink.util.MonitorMetricUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.oppo.cloud.flink.constant.MonitorMetricConstant.*;


/**
 * If there is a delay, and the number of cores is greater than the total number of single TM slots,
 * and the CPU utilization rate cannot be increased, increase the slot number by 1, and at the same
 * time increase the parallelism proportionally, with the parallelism not exceeding the CPU.
 */
@Component
@Slf4j
public class DelayAndCpuNotFullUtilization extends BaseRule {

    @Autowired
    DiagnosisParamsConstants cons;

    @Autowired
    DoctorUtil doctorUtil;

    @Resource
    MonitorMetricUtil monitorMetricUtil;

    @Autowired
    FlinkDiagnosisMetricsServiceImpl flinkDiagnosisMetricsServiceImpl;

    @Autowired
    MemTurningByUsage memTurningByUsage;

    @Override
    public RcJobDiagnosisAdvice advice(DiagnosisContext context) {
        RcJobDiagnosis rcJobDiagnosis = context.getRcJobDiagnosis();
        RcJobDiagnosisAdvice.RcJobDiagnosisAdviceBuilder builder = getBuilder(context);
        builder.adviceType(FlinkRule.ParallelIncr);
        // If there is a delay in the task and the delay needs to continue for more than 10 minutes, it is necessary to perform deburring.
        List<MetricResult.DataResult> delayTimeLagList = context.getMetrics().get(MAX_TIME_LAG_PROMQL);
        if (delayTimeLagList == null || delayTimeLagList.size() == 0) {
            return builder
                    .adviceDescription("delay time is empty")
                    .build();
        }
        // Get the number of indicator points with delays exceeding the threshold.
        long delayHighCount = monitorMetricUtil.getFlatKeyValueStream(delayTimeLagList.get(0))
                .get()
                .filter(Objects::nonNull)
                .filter(x -> {
                    return (x.getValue() != null && x.getValue() > cons.JOB_CUT_LAG_TIME_THRESHOLD);
                })
                .count();
        // Get the maximum delay
        double maxDelay = monitorMetricUtil.getFlatKeyValueStream(delayTimeLagList.get(0))
                .get()
                .map(MetricResult.KeyValue::getValue)
                .filter(Objects::nonNull)
                .mapToDouble(x -> x)
                .max()
                .orElse(0d);
        int step = monitorMetricUtil.getStep(context.getStart(), context.getEnd());
        int countThreshold = (int) Math.ceil(10d * 60d / step);
        boolean isDelay = (delayHighCount > countThreshold);
        Boolean delayLittleHigh = (maxDelay > cons.JOB_DELAY_LITTLE_HIGH);
        log.debug("{} {}-{} job maximum delay {} second", rcJobDiagnosis.getJobName(), context.getStart(),
                context.getEnd(), maxDelay);
        // Delay is continuous within the past 10 minutes.
        Boolean offsetGrow10minutes = offsetGrow10minutes(context);
        Double cpuHighThreshold = doctorUtil.getCpuHighThreshold(context);
        boolean cpuNotHigh = notNullLt(rcJobDiagnosis.getTmAvgCpuUsageAvg() / rcJobDiagnosis.getTmCore(),
                cpuHighThreshold.floatValue());
        Boolean slotLtCpu = rcJobDiagnosis.getTmSlotNum() < rcJobDiagnosis.getTmCore();
        int maxParallel = cons.maxParallel;
        Integer sourcePartitionNumObj = rcJobDiagnosis.getKafkaConsumePartitionNum();
        if (sourcePartitionNumObj == null) {
            log.info("{} can not get source partition num", rcJobDiagnosis.getJobName());
        } else {
            maxParallel = 4 * sourcePartitionNumObj;
        }
        if ((isDelay || (offsetGrow10minutes && delayLittleHigh)) && cpuNotHigh) {
            // Increase concurrency, adjust CPU, and adjust memory.
            int oriSlotNum = rcJobDiagnosis.getTmSlotNum();
            int newSlotNum = oriSlotNum;
            int oriTmMem = rcJobDiagnosis.getTmMem();
            int newTmMem = oriTmMem;

            // Adjust the slot number to ensure sufficient memory.
            while (newSlotNum > 1) {
                // Calculate whether the memory meets the requirements.
                TurningAdvice newMemAdvice = memTurningByUsage.turning(context, newSlotNum);
                if (newMemAdvice == null || newMemAdvice.getTmMem() == null) {
                    newTmMem = oriTmMem * (int) Math.floor((double) newSlotNum / oriSlotNum);
                    newTmMem = newTmMem / 1024 * 1024;
                } else {
                    newTmMem = newMemAdvice.getTmMem();
                }
                // Memory adjustment should be conservative to avoid OOM
                if (newTmMem + 1024 <= cons.tmMemMax) {
                    newTmMem = newTmMem + 1024;
                }
                if (newTmMem >= cons.tmMemMin && newTmMem <= cons.tmMemMax) {
                    break;
                }
                newSlotNum--;
            }
            if (newSlotNum < 1) {
                newSlotNum = 1;
            }
            if (newTmMem > cons.tmMemMax || newTmMem < cons.tmMemMin) {
                newTmMem = cons.tmMemMax;
            }
            // If the concurrency is less than the maximum concurrency, increase the concurrency.
            int newParallel = rcJobDiagnosis.getParallel();
            if (rcJobDiagnosis.getParallel() < maxParallel) {
                int iterateParallel = maxParallel;
                while ((int) Math.ceil((double) iterateParallel / 2) > rcJobDiagnosis.getParallel()) {
                    iterateParallel = (int) Math.ceil((double) iterateParallel / 2);
                }
                newParallel = iterateParallel;
            } else {
                // If the concurrency is already at its maximum, try reducing the slot number and increasing the number of tm.
                if (newSlotNum >= oriSlotNum && oriSlotNum > 1) {
                    newSlotNum = oriSlotNum - 1;
                }
            }
            // Set the CPU to equal the slot.
            int newCpu = newSlotNum;
            int newTmNum = (int) Math.ceil((double) newParallel / newSlotNum);
            int oriTmNum = rcJobDiagnosis.getTmNum();
            int oriCpu = rcJobDiagnosis.getTmCore();
            int oriParallel = rcJobDiagnosis.getParallel();
            if (
                    newTmNum != oriTmNum ||
                            newCpu != oriCpu ||
                            newParallel != oriParallel ||
                            newTmMem != oriTmMem ||
                            newSlotNum != oriSlotNum
            ) {

                RcJobDiagnosisAdvice build = builder.hasAdvice(true)
                        .diagnosisTmNum(newTmNum)
                        .diagnosisTmCore(newCpu)
                        .diagnosisParallel(newParallel)
                        .diagnosisTmMem(newTmMem)
                        .diagnosisTmSlotNum(newSlotNum)
                        .adviceDescription(
                                String.format("作业出现延迟%.2f second,且cpu平均利用率%.2f%%不超过阈值%.2f%%",
                                        maxDelay, rcJobDiagnosis.getTmAvgCpuUsageAvg() / rcJobDiagnosis.getTmCore() * 100, cpuHighThreshold * 100))
                        .build();
                convertAdviceToRcJobDiagnosis(build, context);
                String resourceChange = buildResourceChange(context);
                String conclusion = String.format("作业出现延迟%.2f second,且cpu平均利用率%.2f%%不超过阈值%.2f%%,%s",
                        maxDelay, rcJobDiagnosis.getTmAvgCpuUsageAvg() / rcJobDiagnosis.getTmCore() * 100, cpuHighThreshold * 100, resourceChange);
                DiagnosisRuleReport diagnosisRuleReport = new DiagnosisRuleReport();
                diagnosisRuleReport.setTitle("并行度不足分析");
                diagnosisRuleReport.setConclusion(conclusion);
                DiagnosisRuleLineChart diagnosisRuleLineChartDelay = new DiagnosisRuleLineChart();
                diagnosisRuleLineChartDelay.setTitle("作业延迟");
                diagnosisRuleLineChartDelay.setYAxisUnit("(秒)");
                diagnosisRuleLineChartDelay.setYAxisValueType(DiagnosisReportYAxisType.Second.name());
                DiagnosisRuleLine line = new DiagnosisRuleLine();
                line.setLabel("作业延迟");
                line.setData(delayTimeLagList);
                diagnosisRuleLineChartDelay.setLine(line);
                Map<String, Double> constLine = new HashMap<>();
                constLine.put("阈值", cons.JOB_DELAY_LITTLE_HIGH.doubleValue());
                diagnosisRuleLineChartDelay.setConstLines(constLine);
                DiagnosisRuleLineChart diagnosisRuleLineChartCpu = new DiagnosisRuleLineChart();
                diagnosisRuleLineChartCpu.setTitle("作业CPU使用率");
                diagnosisRuleLineChartCpu.setYAxisUnit("(%)");
                diagnosisRuleLineChartCpu.setYAxisValueType(DiagnosisReportYAxisType.Percent.name());
                DiagnosisRuleLine cpuLine = new DiagnosisRuleLine();
                List<MetricResult.DataResult> cpuUsageList = context.getMetrics().get(TM_AVG_CPU_USAGE_RATE);
                cpuLine.setLabel("作业CPU使用率");
                cpuLine.setData(cpuUsageList);
                diagnosisRuleLineChartCpu.setLine(cpuLine);
                Map<String, Double> constLineCpu = new HashMap<>();
                constLineCpu.put("阈值", cpuHighThreshold);
                constLineCpu.put("均值", (double) (rcJobDiagnosis.getTmAvgCpuUsageAvg() / rcJobDiagnosis.getTmCore()));
                diagnosisRuleLineChartCpu.setConstLines(constLineCpu);
                diagnosisRuleReport.setIDiagnosisRuleCharts(Lists.newArrayList(diagnosisRuleLineChartDelay,
                        diagnosisRuleLineChartCpu));
                build.setDiagnosisRuleReport(diagnosisRuleReport);
                return build;
            } else {
                return builder
                        .adviceDescription(
                                String.format("作业出现延迟%.2f second,且cpu最大利用率%.2f%%不超过阈值%.2f%%,没有合适参数",
                                        maxDelay,
                                        rcJobDiagnosis.getTmAvgCpuUsageAvg() / rcJobDiagnosis.getTmCore() * 100, cpuHighThreshold * 100))
                        .build();
            }

        } else {
            log.debug("{} GrowSlotRule isDelay:{} offsetGrow10minutes:{} cpuNotHigh:{} slotLtCpu:{}",
                    rcJobDiagnosis.getJobName(), isDelay, offsetGrow10minutes, cpuNotHigh, slotLtCpu);
        }
        return builder
                .adviceDescription("No advice")
                .build();
    }

    /**
     * Determine whether the job's recent 10-minute offset has been continuously increasing.
     *
     * @param context
     * @return
     */
    private boolean offsetGrow10minutes(DiagnosisContext context) {
        List<MetricResult.DataResult> offsetDeltaMetrics = flinkDiagnosisMetricsServiceImpl.getTaskManagerMetrics(OFFSET_DELTA, context, context.getStart(), context.getEnd());
        if (offsetDeltaMetrics == null || offsetDeltaMetrics.size() != 1) {
            log.info("{} job offset delta 列表数量不为1", context.getRcJobDiagnosis().getJobName());
            return false;
        }
        // Obtain the point at which the delay began to increase.
        Optional<Integer> ts = monitorMetricUtil.getKeyValueStream(offsetDeltaMetrics.get(0))
                .get()
                .map(MetricResult.KeyValue::getTs)
                .max(Integer::compareTo);
        if (!ts.isPresent()) {
            log.error("{} failed to get ts", context.getRcJobDiagnosis().getJobName());
            return false;
        }
        Integer maxTs = ts.get();
        List<MetricResult.KeyValue> latest10minOffsetDelta = monitorMetricUtil.getKeyValueStream(offsetDeltaMetrics.get(0))
                .get()
                .filter(x -> {
                    return x.getTs() > maxTs - 10 * 60;
                })
                .collect(Collectors.toList());
        Optional<Double> minOffsetDelta = latest10minOffsetDelta
                .stream()
                .map(MetricResult.KeyValue::getValue)
                .min(Double::compareTo);
        if (!minOffsetDelta.isPresent()) {
            log.error("{} failed to get offset delta", context.getRcJobDiagnosis().getJobName());
            return false;
        }
        if (minOffsetDelta.get() > 0) {
            log.info("{}： Continuous 10-minute offset increase {}", context.getRcJobDiagnosis().getJobName(),
                    latest10minOffsetDelta);
            return true;
        } else {
            log.info("{}: No continuous 10-minute offset increase {}", context.getRcJobDiagnosis().getJobName(),
                    latest10minOffsetDelta);
            return false;
        }
    }

}
