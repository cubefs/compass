package com.oppo.cloud.diagnosis.advice.turning;

import com.oppo.cloud.diagnosis.constant.DiagnosisParamsConstants;
import com.oppo.cloud.common.domain.flink.enums.EDiagnosisTurningStatus;
import com.oppo.cloud.diagnosis.domain.diagnosis.DiagnosisContext;
import com.oppo.cloud.diagnosis.domain.diagnosis.RcJobDiagnosis;
import com.oppo.cloud.common.domain.flink.metric.MetricResult;
import com.oppo.cloud.diagnosis.util.DoctorUtil;
import com.oppo.cloud.diagnosis.util.MonitorMetricUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static com.oppo.cloud.diagnosis.constant.MonitorMetricConstant.TM_CPU_USAGE_RATE;


/**
 * 根据并行度调低cpu
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
            log.debug("cpu优化,环境为空");
            resAdvice.setDescription("并行度缩减策略不适用，环境为空");
            return resAdvice;
        }
        Double lowTarget = doctorUtil.getCpuLowTarget(context);
        Double changeRate = null;
        RcJobDiagnosis rcJobDiagnosis = context.getRcJobDiagnosis();
        // 计算缩减率
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
                log.info(context.getRcJobDiagnosis().getJobName() + "目标缩减率:" + changeRate);
            }
        }
        if (changeRate == null) {
            changeRate = cons.getParallelCutRate();
        }
        TurningAdvice advice = new TurningAdvice();
        advice.setTmNum(rcJobDiagnosis.getTmNum());
        int newTmNum = (int) Math.ceil(advice.getTmNum() * (1 - changeRate));
        if (newTmNum == rcJobDiagnosis.getTmNum()) {
            resAdvice.setDescription("并行度缩减策略不适用，tm number没有合适值");
            return resAdvice;
        }
        if (newTmNum < 1) {
            newTmNum = 1;
        }
        int newParallel = newTmNum * context.getRcJobDiagnosis().getTmSlotNum();
        // check 检查是否满足缩减并行度的条件
        Integer sourcePartitionNumObj = context.getRcJobDiagnosis().getKafkaConsumePartitionNum();
        if (sourcePartitionNumObj == null || sourcePartitionNumObj == 0) {
            resAdvice.setDescription("并行度缩减策略不适用，拿不到source partition num");
            return resAdvice;
        }
        int sourcePartitionNum = sourcePartitionNumObj;
        if (sourcePartitionNum > newParallel && sourcePartitionNum % newParallel < newParallel / 2) {
            // 造成数据倾斜了
            resAdvice.setDescription("并行度缩减策略不适用，缩减并行度后会数据倾斜");
            return resAdvice;
        }
        advice.setParallel(newParallel);
        advice.setTmNum(newTmNum);
        advice.setStatus(EDiagnosisTurningStatus.HAS_ADVICE);
        return advice;
    }
}
