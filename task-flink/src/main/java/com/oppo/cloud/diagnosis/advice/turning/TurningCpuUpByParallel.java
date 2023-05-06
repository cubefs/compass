package com.oppo.cloud.diagnosis.advice.turning;

import com.oppo.cloud.diagnosis.constant.DiagnosisParamsConstants;
import com.oppo.cloud.common.domain.flink.enums.DiagnosisTurningStatus;
import com.oppo.cloud.diagnosis.domain.diagnosis.DiagnosisContext;
import com.oppo.cloud.diagnosis.domain.diagnosis.RcJobDiagnosis;
import com.oppo.cloud.diagnosis.util.DoctorUtil;
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
        int sourcePartitionNum = context.getRcJobDiagnosis().getKafkaConsumePartitionNum();
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
