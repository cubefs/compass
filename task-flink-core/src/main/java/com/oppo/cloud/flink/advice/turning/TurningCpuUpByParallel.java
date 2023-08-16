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
import com.oppo.cloud.flink.util.DoctorUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 根据并行度调高cpu
 */
@Slf4j
@Component
public class TurningCpuUpByParallel implements TurningCpuUpStrategy {
    @Autowired
    DiagnosisParamsConstants cons;
    @Autowired
    DoctorUtil doctorUtil ;

    @Override
    public TurningAdvice turning(DiagnosisContext context) {
        TurningAdvice resAdvice = new TurningAdvice();
        if (context == null || context.getRcJobDiagnosis() == null) {
            log.debug("cpu优化,环境为空");
            resAdvice.setDescription("并行度扩容策略不适用,环境为空");
            return resAdvice;
        }
        RcJobDiagnosis rcJobDiagnosis = context.getRcJobDiagnosis();
        Double changeRate = cons.getTmParallelGrowRate();
        // 根据cpu利用率计算change rate
        Double jobChangeRate = doctorUtil.getGrowChangeRate(context);
        if (jobChangeRate != null) {
            changeRate = jobChangeRate;
        }
        log.debug("{} job change rate {}", rcJobDiagnosis.getJobName(), changeRate);
        int newTmNum = (int) Math.ceil(rcJobDiagnosis.getTmNum() * (1 + changeRate));
        int newParallel = newTmNum * rcJobDiagnosis.getTmSlotNum();
        Integer sourcePartitionNum = context.getRcJobDiagnosis().getKafkaConsumePartitionNum();
        if(sourcePartitionNum == null){
            resAdvice.setDescription("并行度扩容策略不适用,分区为空");
            return resAdvice;
        }
        if(rcJobDiagnosis.getParallel() >= sourcePartitionNum){
            resAdvice.setDescription("并行度扩容策略不适用,并行度已经大于等于source partition总数");
            return resAdvice;
        }
        if (newParallel > sourcePartitionNum) {
            newParallel = sourcePartitionNum;
        }else{
            int upJumpNewParallel = sourcePartitionNum;
            // 并行度调整为分区的倍数,防止数据倾斜
            while ((int) Math.ceil((double) upJumpNewParallel / 2) > newParallel) {
                upJumpNewParallel = (int) Math.ceil((double) upJumpNewParallel / 2);
            }
            newParallel = upJumpNewParallel;
        }

        resAdvice.setTmNum(newTmNum);
        resAdvice.setParallel(newParallel);
        resAdvice.setStatus(DiagnosisTurningStatus.HAS_ADVICE);
        return resAdvice;
    }
}
