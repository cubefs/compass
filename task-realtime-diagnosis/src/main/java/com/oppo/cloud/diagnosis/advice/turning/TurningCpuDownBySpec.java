package com.oppo.cloud.diagnosis.advice.turning;

import com.oppo.cloud.diagnosis.constant.DiagnosisParamsConstants;
import com.oppo.cloud.common.domain.flink.enums.EDiagnosisTurningStatus;
import com.oppo.cloud.diagnosis.domain.diagnosis.DiagnosisContext;
import com.oppo.cloud.diagnosis.domain.diagnosis.RcJobDiagnosis;
import com.oppo.cloud.common.domain.flink.metric.MetricResult;
import com.oppo.cloud.diagnosis.util.DoctorUtil;
import com.oppo.cloud.diagnosis.util.MonitorMetricUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.oppo.cloud.diagnosis.constant.MonitorMetricConstant.TM_CPU_USAGE_RATE;


/**
 * 根据规格调低cpu
 */
@Component
@Data
@Slf4j
public class TurningCpuDownBySpec implements TurningCpuDownStrategy {
    @Autowired
    DoctorUtil doctorUtil;
    @Autowired
    MonitorMetricUtil monitorMetricUtil;
    @Autowired
    DiagnosisParamsConstants cons;
    @Autowired
    MemTurningByUsage memTurning;

    @Override
    public TurningAdvice turning(DiagnosisContext context) {
        Double lowTarget = doctorUtil.getCpuLowTarget(context);
        TurningAdvice resAdvice = new TurningAdvice();
        Double changeRate = null;
        RcJobDiagnosis rcJobDiagnosis = context.getRcJobDiagnosis();
        int parallel = rcJobDiagnosis.getParallel();
        int vcore = rcJobDiagnosis.getTmCore();
        int tmSlotNum = rcJobDiagnosis.getTmSlotNum();
        if (tmSlotNum == 0 || parallel == 0) {
            resAdvice.setDescription("cpu缩减策略不适用，tm slot为0或者parallel为0");
            return resAdvice;
        }
        // 计算缩减率
        List<MetricResult.DataResult> cpuUsageList = context.getMetrics().get(TM_CPU_USAGE_RATE);
        if (cpuUsageList != null && cpuUsageList.size() > 0) {
            Double maxCpuUsage = cpuUsageList.stream()
                    .map(x -> monitorMetricUtil.getSmoothMaxOrNull(x, 3))
                    .filter(Objects::nonNull)
                    .max(Double::compareTo)
                    .orElse(Double.MAX_VALUE);
            if (maxCpuUsage > 0 && maxCpuUsage / rcJobDiagnosis.getTmCore() < lowTarget) {
                double unitMaxCpuUsage = maxCpuUsage / rcJobDiagnosis.getTmCore();
                changeRate = 1 - unitMaxCpuUsage / lowTarget;
                log.info(context.getRcJobDiagnosis().getJobName() + "目标缩减率:" + changeRate);
            }
        }
        if (changeRate == null) {
            changeRate = cons.getParallelCutRate();
        }
        int tmNum = rcJobDiagnosis.getTmNum();
        int oriTotalCores = tmNum * vcore;
        int oriTotalSlot = tmNum * tmSlotNum;
        int oriMem = rcJobDiagnosis.getTmMem();
        ArrayList<TurningAdvice> advices = new ArrayList<>();
        Integer partitions = context.getRcJobDiagnosis().getKafkaConsumePartitionNum();
        int maxSlot = Math.min(parallel, 6);
        if (partitions != null && !partitions.equals(0)) {
            maxSlot = Math.min(maxSlot, partitions);
        }
        for (int tmSlotNumIndex = 1; tmSlotNumIndex <= maxSlot; tmSlotNumIndex++) {
            TurningAdvice advice = calcAdvice(tmSlotNumIndex, tmSlotNum, parallel, oriTotalCores, oriTotalSlot, tmNum, vcore, oriMem, changeRate, context);
            if (advice != null) {
                advices.add(advice);
                log.debug(advice.toString());
            }
        }
        TurningAdvice advice = advices.stream().max((o1, o2) -> Float.compare(o1.getScore(), o2.getScore())).orElse(null);
        if (advice == null) {
            resAdvice.setDescription("cpu缩减策略不适用，没有合适的cpu方案");
            return resAdvice;
        } else {
            return advice;
        }
    }

    public TurningAdvice calcAdvice(int newSlotNum, int oriSlotNum, int oriParallel, int oriTotalCore, int oriTotalSlot, int oriTmNum, int oriVcore, int oriMem, Double cutRate, DiagnosisContext context) {

        int newTmNum = (int) Math.ceil((double) oriParallel / newSlotNum);
        int newTotalSlot = newTmNum * newSlotNum;
        int maxNewCore = (int) Math.floor((double) oriTotalCore / newTmNum);
        ArrayList<TurningAdvice> advices = new ArrayList<>();
        for (int vcoreIndex = maxNewCore; vcoreIndex > 0; vcoreIndex--) {
            // 计算是否总体core减少
            int newTotalCore = vcoreIndex * newTmNum;
            // 这里新的 core 可以等于旧的core，因为内存可以缩小
            if (newTotalCore > oriTotalCore) {
                continue;
            }
            // 计算内存是否符合要求
            TurningAdvice newMemAdvice = memTurning.turning(context, newSlotNum);
            if (newMemAdvice == null) {
                continue;
            }
            Integer newMem = newMemAdvice.getTmMem();
            if (newMem == null) {
                newMem = oriMem * (int) Math.floor((double) newSlotNum / oriSlotNum);
                newMem = newMem / 1024 * 1024;
            }
            if (newMem > cons.tmMemMax || newMem < cons.tmMemMin) {
                continue;
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
            double desChangeRate = cutRate;
            float realChangeRate = (float) (oriTotalCore - newTotalCore) / oriTotalCore;
            // 缩减资源时候，不能大于目标缩减率
            if (realChangeRate < desChangeRate) {
                float score = 100 * (1 - Math.abs(realChangeRate - (float) desChangeRate));
                advice.setScore(score);
                if (score > 0 && realChangeRate > 0) {
                    advice.setStatus(EDiagnosisTurningStatus.HAS_ADVICE);
                    advices.add(advice);
                    log.debug(advice.toString());
                }
            }
        }
        if (advices.size() == 0) {
            return null;
        }
        Optional<TurningAdvice> max = advices.stream().max((o1, o2) -> Float.compare(o1.getScore(), o2.getScore()));
        return max.orElse(null);
    }

}
