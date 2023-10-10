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

package com.oppo.cloud.flink.advice.turning;

import com.oppo.cloud.flink.advice.rule.TmManagedMemory;
import com.oppo.cloud.flink.constant.DiagnosisParamsConstants;
import com.oppo.cloud.flink.domain.diagnosis.DiagnosisContext;
import com.oppo.cloud.flink.domain.diagnosis.RcJobDiagnosisAdvice;
import com.oppo.cloud.common.domain.flink.metric.MetricResult;
import com.oppo.cloud.flink.util.DoctorUtil;
import com.oppo.cloud.flink.util.MonitorMetricUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.oppo.cloud.flink.constant.MonitorMetricConstant.*;


/**
 * Suggest the appropriate amount of memory based on the memory usage situation.
 */
@Component
@Slf4j
public class MemTurningByUsage {

    @Autowired
    DiagnosisParamsConstants cons;

    @Autowired
    DoctorUtil doctorUtil;

    @Autowired
    TmManagedMemory tmManagedMemory;

    @Autowired
    MonitorMetricUtil monitorMetricUtil;

    public TurningAdvice turning(DiagnosisContext context, int newTmSlotNum) {

        if (context == null || context.getRcJobDiagnosis() == null) {
            log.error("Memory optimization, environment is empty.");
            throw new RuntimeException("Memory optimization, environment is empty.");
        }
        Double memHighTarget = doctorUtil.getMemHighTarget(context);
        // Average heap memory usage
        List<MetricResult.DataResult> heapUsageList = context.getMetrics().get(TM_USAGE_HEAP_MEM_MAX);
        if (heapUsageList == null) {
            return calcMemBySlot(context, newTmSlotNum);
        }
        Optional<Double> maxHeapUsageAvg = heapUsageList.stream().map(monitorMetricUtil::getAvg).max(Double::compareTo);
        if (!maxHeapUsageAvg.isPresent()) {
            return calcMemBySlot(context, newTmSlotNum);
        }
        // The maximum value of average heap usage in megabytes in TM.
        double heapUsageAvg = maxHeapUsageAvg.get() / 1024 / 1024;
        // Total heap memory size.
        List<MetricResult.DataResult> heapTotalList = context.getMetrics().get(TM_TOTAL_HEAP_MEM_MAX);
        if (heapTotalList == null) {
            return calcMemBySlot(context, newTmSlotNum);
        }
        Optional<Double> heapTotalListOption = heapTotalList.stream().map(monitorMetricUtil::getMaxOrNull).filter(Objects::nonNull).max(Double::compareTo);
        if (!heapTotalListOption.isPresent()) {
            return calcMemBySlot(context, newTmSlotNum);
        }
        // Total heap size in megabytes.
        double heapTotal = heapTotalListOption.get() / 1024 / 1024;
        // Total managed memory size in megabytes.
        double manageTotal = 0;
        RcJobDiagnosisAdvice manageMemoryAdvice = tmManagedMemory.advice(context);
        // If there is a suggested value for managed memory, use it for calculation.
        if (manageMemoryAdvice != null && manageMemoryAdvice.getHasAdvice() == true) {
            manageTotal = manageMemoryAdvice.getDiagnosisManageMem();
        } else {
            // Otherwise, use the current value of managed memory for calculation.
            List<MetricResult.DataResult> manageTotalList = context.getMetrics().get(TM_MANAGE_MEM_TOTAL);
            if (manageTotalList == null) {
                log.info(String.format("%s: memory optimization for the job, manageTotalList is empty.", context.getRcJobDiagnosis().getJobName()));
            } else {
                Optional<Double> manageTotalListOption = manageTotalList.stream().map(monitorMetricUtil::getMaxOrNull).filter(Objects::nonNull).max(Double::compareTo);
                if (!manageTotalListOption.isPresent()) {
                    log.info(String.format("%s: memory optimization for the job, manageTotalListOption is empty.", context.getRcJobDiagnosis().getJobName()));
                } else {
                    manageTotal = manageTotalListOption.get() / 1024 / 1024;
                }
            }
        }

        int tmMem = context.getRcJobDiagnosis().getTmMem();
        int oriTmSlotNum = context.getRcJobDiagnosis().getTmSlotNum();
        double otherMem = tmMem - manageTotal - heapTotal;
        double needHeapMemAvg = heapUsageAvg * newTmSlotNum / oriTmSlotNum;
        double newHeapMemTotal = needHeapMemAvg / memHighTarget;
        double newManageTotal = manageTotal * (newHeapMemTotal / heapTotal);
        double tmTotalMem = newHeapMemTotal + newManageTotal + otherMem;
        tmTotalMem = Math.ceil(tmTotalMem / 1024) * 1024;
        TurningAdvice res = new TurningAdvice();
        res.setTmMem((int) tmTotalMem);
        log.debug(String.format("Adjust memory usage based on actual usage:tm_mem:%dMB,originTmSlotNum:%d,newTmSlotNum:%d,"
                + "manageTotal:%.4f,heapTotal:%.4f,otherMem:%.4f,"
                + "heapUsageAvg:%.4f,needHeapMemAvg:%.4f,newHeapMemTotal:%.4f,"
                + "newManageTotal:%.4f,tmTotalMem:%.4f", tmMem, oriTmSlotNum, newTmSlotNum, manageTotal, heapTotal,
                otherMem, heapUsageAvg, needHeapMemAvg, newHeapMemTotal, newManageTotal, tmTotalMem));
        return res;
    }

    private TurningAdvice calcMemBySlot(DiagnosisContext context, int newTmSlotNum) {
        Integer mem = context.getRcJobDiagnosis().getTmMem();
        double newMem = (double) mem / context.getRcJobDiagnosis().getTmSlotNum() * newTmSlotNum;
        newMem = Math.ceil(newMem / 1024) * 1024;
        TurningAdvice res = new TurningAdvice();
        res.setTmMem((int) newMem);
        return res;
    }
}
