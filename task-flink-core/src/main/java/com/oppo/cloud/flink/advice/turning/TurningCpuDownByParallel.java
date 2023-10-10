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

import com.oppo.cloud.flink.constant.DiagnosisParamsConstants;
import com.oppo.cloud.common.domain.flink.enums.DiagnosisTurningStatus;
import com.oppo.cloud.flink.domain.diagnosis.DiagnosisContext;
import com.oppo.cloud.flink.domain.diagnosis.RcJobDiagnosis;
import com.oppo.cloud.common.domain.flink.metric.MetricResult;
import com.oppo.cloud.flink.util.DoctorUtil;
import com.oppo.cloud.flink.util.MonitorMetricUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static com.oppo.cloud.flink.constant.MonitorMetricConstant.TM_CPU_USAGE_RATE;


/**
 * Adjust CPU usage based on parallelism.
 */
@Slf4j
@Component
public class TurningCpuDownByParallel implements TurningCpuDownStrategy {

    @Autowired
    DiagnosisParamsConstants cons;

    @Autowired
    DoctorUtil doctorUtil ;

    @Autowired
    MonitorMetricUtil monitorMetricUtil;

    @Override
    public TurningAdvice turning(DiagnosisContext context) {
        TurningAdvice resAdvice = new TurningAdvice();
        if (context == null || context.getRcJobDiagnosis() == null) {
            log.debug("CPU optimization, environment is empty.");
            resAdvice.setDescription("Parallelism reduction strategy does not apply, environment is empty.");
            return resAdvice;
        }
        Double lowTarget = doctorUtil.getCpuLowTarget(context);
        Double changeRate = null;
        RcJobDiagnosis rcJobDiagnosis = context.getRcJobDiagnosis();
        // Calculate the reduction rate.
        List<MetricResult.DataResult> cpuUsageList = context.getMetrics().get(TM_CPU_USAGE_RATE);
        if (cpuUsageList != null && cpuUsageList.size() > 0) {
            Double maxCpuUsage = cpuUsageList.stream()
                    .map(x -> monitorMetricUtil.getSmoothMaxOrNull(x, 3))
                    .filter(Objects::nonNull)
                    .max(Double::compareTo)
                    .orElse(Double.MAX_VALUE);
            if (maxCpuUsage < lowTarget) {
                double unitMaxCpuUsage = maxCpuUsage;
                changeRate = 1 - unitMaxCpuUsage / lowTarget;
                log.info(context.getRcJobDiagnosis().getJobName() + " target reduction rate:" + changeRate);
            }
        }
        if (changeRate == null) {
            changeRate = cons.getParallelCutRate();
        }
        TurningAdvice advice = new TurningAdvice();
        advice.setTmNum(rcJobDiagnosis.getTmNum());
        int newTmNum = (int) Math.ceil(advice.getTmNum() * (1 - changeRate));
        if (newTmNum == rcJobDiagnosis.getTmNum()) {
            resAdvice.setDescription("Parallelism reduction strategy does not apply, there is no suitable value for TM number.");
            return resAdvice;
        }
        if (newTmNum < 1) {
            newTmNum = 1;
        }
        int newParallel = newTmNum * context.getRcJobDiagnosis().getTmSlotNum();
        // Check if the conditions for reducing parallelism are met.
        Integer sourcePartitionNumObj = context.getRcJobDiagnosis().getKafkaConsumePartitionNum();
        if (sourcePartitionNumObj == null || sourcePartitionNumObj == 0) {
            resAdvice.setDescription("Parallelism reduction strategy does not apply, unable to obtain source partition num.");
            return resAdvice;
        }
        int sourcePartitionNum = sourcePartitionNumObj;
        if (sourcePartitionNum > newParallel && sourcePartitionNum % newParallel < newParallel / 2) {
            // It has caused data skew.
            resAdvice.setDescription("The strategy of reducing parallelism is not applicable, and data skew may occur after reducing parallelism.");
            return resAdvice;
        }
        advice.setParallel(newParallel);
        advice.setTmNum(newTmNum);
        advice.setStatus(DiagnosisTurningStatus.HAS_ADVICE);
        return advice;
    }
}
