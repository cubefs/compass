package com.oppo.cloud.diagnosis.advice.turning;

import com.oppo.cloud.diagnosis.constant.DiagnosisParamsConstants;
import com.oppo.cloud.common.domain.flink.enums.DiagnosisTurningStatus;
import com.oppo.cloud.diagnosis.domain.diagnosis.DiagnosisContext;
import com.oppo.cloud.diagnosis.domain.diagnosis.RcJobDiagnosis;
import com.oppo.cloud.diagnosis.util.DoctorUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 根据规则扩容cpu
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
        // 根据cpu利用率计算change rate
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
        log.debug("{} 计算cpu方案 new Slot Num:{},oriSlotNum:{}", context.getRcJobDiagnosis().getJobName(), newSlotNum, oriSlotNum);
        for (int vcoreIndex = minNewCore; vcoreIndex < minNewCore + 10; vcoreIndex++) {
            // 计算是否总体core减少
            int newTotalCore = vcoreIndex * newTmNum;
            if (newTotalCore <= oriTotalCore) {
                log.debug("{} newTotalCore {} <= oriTotalCore {}", context.getRcJobDiagnosis().getJobName(), newTotalCore, oriTotalCore);
                continue;
            }
            // 计算内存是否符合要求
            TurningAdvice newMemAdvice = memTurning.turning(context, newSlotNum);
            if (newMemAdvice == null) {
                log.debug("{} 内存为null", context.getRcJobDiagnosis().getJobName());
                continue;
            }
            Integer newMem = newMemAdvice.getTmMem();
            if (newMem == null) {
                newMem = oriMem * (int) Math.floor((double) newSlotNum / oriSlotNum);
                newMem = newMem / 1024 * 1024;
            }
            if (newMem > cons.tmMemMax || newMem < cons.tmMemMin) {
                log.debug("{} {} 内存超出阈值", context.getRcJobDiagnosis().getJobName(), newMem);
                // slot 等于1
                if (newSlotNum == 1) {
                    if (oriMem <= cons.tmMemMax) {
                        // 任务原始tm内存在范围内，且新的slot为1已经是最小值，此时尽管计算的内存超了，仍然设置为内存最大值，因为此时是内存最大配比
                        log.debug("{} 内存设置为和ori mem {}相等", context.getRcJobDiagnosis().getJobName(), oriMem);
                        newMem = cons.tmMemMax;
                    } else {
                        newMem = oriMem;
                    }
                } else {
                    log.debug("{} new mem {}, ori mem {}", context.getRcJobDiagnosis().getJobName(), newMem, oriMem);
                    continue;
                }
            }
            // 内存调整要尽量保守，防止oom
            if (newMem + 1024 <= cons.tmMemMax) {
                newMem = newMem + 1024;
            }
            // 生成建议对象,计算得分
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
                // oneDivideExp 取值范围是1-0,所以这里score 是 100-50
                score = 50 + 50 * doctorUtil.oneDivideExp(realChangeRate - changeRate);
            } else {
                // 这里score取值范围是50-5 realChangeRate=changeRate 时候 score 是50,realChangeRate < changeRate 时候,score 小于50大于0
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
            log.debug("{} advices 为空 ", context.getRcJobDiagnosis().getJobName());
            return null;
        }
        Optional<TurningAdvice> max = advices.stream().max((o1, o2) -> Float.compare(o1.getScore(), o2.getScore()));
        return max.orElse(null);
    }
}
