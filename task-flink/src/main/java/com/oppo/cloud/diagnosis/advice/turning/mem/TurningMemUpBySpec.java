package com.oppo.cloud.diagnosis.advice.turning.mem;

import com.oppo.cloud.diagnosis.advice.turning.MemTurningByUsage;
import com.oppo.cloud.diagnosis.advice.turning.TurningAdvice;
import com.oppo.cloud.diagnosis.constant.DiagnosisParamsConstants;
import com.oppo.cloud.common.domain.flink.enums.DiagnosisTurningStatus;
import com.oppo.cloud.diagnosis.domain.diagnosis.DiagnosisContext;
import com.oppo.cloud.diagnosis.domain.diagnosis.RcJobDiagnosis;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 根据规格调高mem
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
            log.debug("内存优化,环境为空");
            resAdvice.setDescription("内存优化,环境为空");
            return resAdvice;
        }
        TurningAdvice adviceMem = memByUsage.turning(context, context.getRcJobDiagnosis().getTmSlotNum());
        if (adviceMem == null) {
            resAdvice.setDescription("内存需求计算返回null");
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
            resAdvice.setDescription("内存需求量和当前量相同");
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
