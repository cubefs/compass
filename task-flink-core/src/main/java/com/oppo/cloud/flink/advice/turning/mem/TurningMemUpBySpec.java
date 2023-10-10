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

package com.oppo.cloud.flink.advice.turning.mem;

import com.oppo.cloud.flink.advice.turning.MemTurningByUsage;
import com.oppo.cloud.flink.advice.turning.TurningAdvice;
import com.oppo.cloud.flink.constant.DiagnosisParamsConstants;
import com.oppo.cloud.common.domain.flink.enums.DiagnosisTurningStatus;
import com.oppo.cloud.flink.domain.diagnosis.DiagnosisContext;
import com.oppo.cloud.flink.domain.diagnosis.RcJobDiagnosis;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Increase memory according to the specifications.
 */
@Component
@Slf4j
public class TurningMemUpBySpec implements TurningMemUpStrategy {

    @Autowired
    DiagnosisParamsConstants cons;

    @Autowired
    MemTurningByUsage memByUsage;

    public TurningAdvice turning(DiagnosisContext context) {
        TurningAdvice resAdvice = new TurningAdvice();
        if (context == null || context.getRcJobDiagnosis() == null) {
            log.debug("Memory optimization, environment is empty.");
            resAdvice.setDescription("Memory optimization, environment is empty.");
            return resAdvice;
        }
        TurningAdvice adviceMem = memByUsage.turning(context, context.getRcJobDiagnosis().getTmSlotNum());
        if (adviceMem == null) {
            resAdvice.setDescription("Memory requirement calculation returns null.");
            return resAdvice;
        }
        int adviceTmMem = adviceMem.getTmMem();
        if (adviceTmMem < cons.tmMemMin) {
            adviceTmMem = cons.tmMemMin;
        }
        if (adviceTmMem > cons.tmMemMax) {
            adviceTmMem = cons.tmMemMax;
        }
        if (adviceTmMem <= context.getRcJobDiagnosis().getTmMem()) {
            resAdvice.setDescription("The memory demand is the same as the current amount.");
            return resAdvice;
        }
        RcJobDiagnosis job = context.getRcJobDiagnosis();
        TurningAdvice advice = new TurningAdvice();
        advice.setStatus(DiagnosisTurningStatus.HAS_ADVICE);
        advice.setParallel(job.getParallel());
        advice.setVcore(job.getTmCore());
        advice.setTmSlotNum(job.getTmSlotNum());
        advice.setTmNum(job.getTmNum());
        advice.setTmMem(adviceTmMem);
        return advice;
    }
}
