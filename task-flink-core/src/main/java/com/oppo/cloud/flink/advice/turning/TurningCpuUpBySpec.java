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

import java.util.*;

/**
 * Increase CPU capacity according to the rules.
 */
@Component
@Slf4j
public class TurningCpuUpBySpec implements TurningCpuUpStrategy {

    @Autowired
    DiagnosisParamsConstants cons;
    @Autowired
    MemTurningByUsage memTurning;
    @Autowired
    DoctorUtil doctorUtil;

    @Override
    public TurningAdvice turning(DiagnosisContext context) {
        TurningAdvice noAdvice = new TurningAdvice();
        RcJobDiagnosis rcJobDiagnosis = context.getRcJobDiagnosis();
        int parallel = rcJobDiagnosis.getParallel();
        int vcore = rcJobDiagnosis.getTmCore();
        int slotNum = rcJobDiagnosis.getTmSlotNum();
        if (slotNum == 0 || parallel == 0) {
            noAdvice.setDescription("cpu扩容策略不适用,slot或者parallel为0");
            return noAdvice;
        }
        Double changeRate = cons.tmParallelGrowRate;
        // Calculate change rate based on CPU utilization rate.
        Double jobChangeRate = doctorUtil.getGrowChangeRate(context);
        if (jobChangeRate != null) {
            changeRate = jobChangeRate;
        }
        log.debug("{} job change rate {}", rcJobDiagnosis.getJobName(), changeRate);
        int tmNum = (int) Math.ceil((double) parallel / slotNum);
        int oriTotalCores = tmNum * vcore;
        int oriTotalSlot = tmNum * slotNum;
        int oriMem = rcJobDiagnosis.getTmMem();
        ArrayList<TurningAdvice> advices = new ArrayList<>();
        for (int slotNumIndex = 1; slotNumIndex <= 6; slotNumIndex++) {
            TurningAdvice advice = calcAdvice(slotNumIndex, slotNum, parallel, oriTotalCores, oriTotalSlot, tmNum, vcore, oriMem, changeRate, context);
            if (advice != null) {
                advices.add(advice);
                log.debug("{} cpu 参数 {}", rcJobDiagnosis.getJobName(), advice);
            } else {
                log.debug("{} cpu 建议为null,new slot{}", rcJobDiagnosis.getJobName(), slotNumIndex);
            }
        }
        TurningAdvice advice = advices.stream().max((o1, o2) -> Float.compare(o1.getScore(), o2.getScore())).orElse(null);
        if (advice == null) {
            log.debug(advices.toString());
            noAdvice.setDescription("cpu扩容策略不适用,没有合适的cpu方案");
            return noAdvice;
        } else {
            return advice;
        }
    }

    public TurningAdvice calcAdvice(int newSlotNum, int oriSlotNum, int oriParallel, int oriTotalCore, int oriTotalSlot, int oriTmNum, int oriVcore, int oriMem, Double changeRateByFlow, DiagnosisContext context) {
        int newTmNum = (int) Math.ceil((double) oriParallel / newSlotNum);
        int newTotalSlot = newTmNum * newSlotNum;
        int minNewCore = (int) Math.ceil((double) oriTotalCore / newTmNum);
        ArrayList<TurningAdvice> advices = new ArrayList<>();
        log.debug("{} calculates the new slot number for CPU scheme:{},oriSlotNum:{}", context.getRcJobDiagnosis().getJobName(), newSlotNum, oriSlotNum);
        for (int vcoreIndex = minNewCore; vcoreIndex < minNewCore + 10; vcoreIndex++) {
            // Calculate if there is an overall decrease in cores.
            int newTotalCore = vcoreIndex * newTmNum;
            if (newTotalCore <= oriTotalCore) {
                log.debug("{} newTotalCore {} <= oriTotalCore {}", context.getRcJobDiagnosis().getJobName(), newTotalCore, oriTotalCore);
                continue;
            }
            // Calculate if the memory meets the requirements.
            TurningAdvice newMemAdvice = memTurning.turning(context, newSlotNum);
            if (newMemAdvice == null) {
                log.debug("{} Memory is null.", context.getRcJobDiagnosis().getJobName());
                continue;
            }
            Integer newMem = newMemAdvice.getTmMem();
            if (newMem == null) {
                newMem = oriMem * (int) Math.floor((double) newSlotNum / oriSlotNum);
                newMem = newMem / 1024 * 1024;
            }
            if (newMem > cons.tmMemMax || newMem < cons.tmMemMin) {
                log.debug("{} {} Memory exceeds the threshold.", context.getRcJobDiagnosis().getJobName(), newMem);
                // slot is 1
                if (newSlotNum == 1) {
                    if (oriMem <= cons.tmMemMax) {
                        // If the original memory of the task is within the range, and the new slot value is already at
                        // the minimum value of 1, even if the calculated memory exceeds the limit, it will still be set
                        // to the maximum value because this is the maximum memory ratio.
                        log.debug("Set the {} memory to be equal to the origin memory {}", context.getRcJobDiagnosis().getJobName(), oriMem);
                        newMem = cons.tmMemMax;
                    } else {
                        newMem = oriMem;
                    }
                } else {
                    log.debug("{} new memory {}, origin memory {}", context.getRcJobDiagnosis().getJobName(), newMem, oriMem);
                    continue;
                }
            }
            // Memory adjustment should be conservative to prevent OOM (Out of Memory).
            if (newMem + 1024 <= cons.tmMemMax) {
                newMem = newMem + 1024;
            }
            // Generate a recommendation object and calculate the score.
            TurningAdvice advice = new TurningAdvice();
            advice.setParallel(oriParallel);
            advice.setTmSlotNum(newSlotNum);
            advice.setTmNum(newTmNum);
            advice.setVcore(vcoreIndex);
            advice.setTotalCore(newTotalCore);
            advice.setTotalSlot(newTotalSlot);
            advice.setTmMem(newMem);
            double changeRate = changeRateByFlow;
            float newSlotCore = (float) vcoreIndex / newSlotNum;
            float oldSlotCore = (float) oriVcore / oriSlotNum;
            double realChangeRate = (double) Math.abs(newSlotCore - oldSlotCore) / oldSlotCore;
            double score;
            if (realChangeRate > changeRate) {
                // If the range of `oneDivideExp` is from 1 to 0, then the score here would be from 100 to 50.
                score = 50 + 50 * doctorUtil.oneDivideExp(realChangeRate - changeRate);
            } else {
                // The score range here is 50 to 5. When realChangeRate equals changeRate, the score is 50.
                // When realChangeRate is less than changeRate, the score is less than 50 but greater than 0.
                score = 50 - 50 * doctorUtil.oneSubExp(realChangeRate - changeRate);
            }
            advice.setScore((float) score);
            log.debug(advice.toString());
            if (realChangeRate > 0) {
                advice.setStatus(DiagnosisTurningStatus.HAS_ADVICE);
                advices.add(advice);
                log.debug(advice.toString());
            } else {
                log.debug("{} real change rate{} < {}", context.getRcJobDiagnosis().getJobName(), realChangeRate, 0);
            }
        }
        if (advices.size() == 0) {
            log.debug("{} advices is empty ", context.getRcJobDiagnosis().getJobName());
            return null;
        }
        Optional<TurningAdvice> max = advices.stream().max((o1, o2) -> Float.compare(o1.getScore(), o2.getScore()));
        return max.orElse(null);
    }
}
