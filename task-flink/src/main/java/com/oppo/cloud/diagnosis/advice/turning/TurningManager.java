package com.oppo.cloud.diagnosis.advice.turning;

import com.google.common.collect.Lists;
import com.oppo.cloud.diagnosis.advice.turning.mem.TurningMemDownBySpec;
import com.oppo.cloud.diagnosis.advice.turning.mem.TurningMemDownStrategy;
import com.oppo.cloud.diagnosis.advice.turning.mem.TurningMemUpBySpec;
import com.oppo.cloud.diagnosis.advice.turning.mem.TurningMemUpStrategy;
import com.oppo.cloud.common.domain.flink.enums.DiagnosisTurningStatus;
import com.oppo.cloud.diagnosis.domain.diagnosis.DiagnosisContext;
import com.oppo.cloud.diagnosis.util.DoctorUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class TurningManager {
    @Autowired
    TurningCpuDownByParallel turningCpuDownByParallel;
    @Autowired
    TurningCpuUpByParallel turningCpuUpByParallel;
    @Autowired
    TurningCpuUpBySpec turningCpuUpBySpec;
    @Autowired
    TurningCpuDownBySpec turningCpuDownBySpec;
    @Autowired
    TurningMemDownBySpec turningMemDownBySpec;
    @Autowired
    TurningMemUpBySpec turningMemUpBySpec;
    @Autowired
    DoctorUtil doctorUtil;

    // 调大cpu
    public TurningAdvice turningCpuUp(DiagnosisContext context) {
        List<TurningCpuUpStrategy> strategies = Lists.newArrayList(turningCpuUpByParallel, turningCpuUpBySpec
        );
        TurningAdvice noAdviceRes = new TurningAdvice();
        for (TurningCpuUpStrategy s : strategies) {
            try {
                TurningAdvice turning = s.turning(context);
                if (turning != null && turning.getStatus() == DiagnosisTurningStatus.HAS_ADVICE) {
                    return turning;
                } else if (turning != null) {
                    noAdviceRes.setDescription(doctorUtil.addComma(noAdviceRes.getDescription())
                            + turning.getDescription());
                }
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            }
        }
        return noAdviceRes;
    }

    // 调小cpu
    public TurningAdvice turningCpuDown(DiagnosisContext context) {
        List<TurningCpuDownStrategy> strategies = Lists.newArrayList(turningCpuDownByParallel, turningCpuDownBySpec
        );
        TurningAdvice noAdviceRes = new TurningAdvice();
        for (TurningCpuDownStrategy s : strategies) {
            try {
                TurningAdvice turning = s.turning(context);
                if (turning != null && turning.getStatus() == DiagnosisTurningStatus.HAS_ADVICE) {
                    return turning;
                } else if (turning != null) {
                    noAdviceRes.setDescription(doctorUtil.addComma(noAdviceRes.getDescription())
                            + turning.getDescription());
                }
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            }
        }
        return noAdviceRes;
    }

    // 调小内存
    public TurningAdvice turningMemDown(DiagnosisContext context) {
        List<TurningMemDownStrategy> strategies = Lists.newArrayList(turningMemDownBySpec);
        TurningAdvice noAdviceRes = new TurningAdvice();
        for (TurningMemDownStrategy s : strategies) {
            try {
                TurningAdvice turning = s.turning(context);
                if (turning != null && turning.getStatus() == DiagnosisTurningStatus.HAS_ADVICE) {
                    return turning;
                } else if (turning != null) {
                    noAdviceRes.setDescription(doctorUtil.addComma(noAdviceRes.getDescription()) +
                            turning.getDescription());
                }
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            }
        }
        return noAdviceRes;
    }
    // 调大内存
    public TurningAdvice turningMemUp(DiagnosisContext context) {
        List<TurningMemUpStrategy> strategies = Lists.newArrayList(turningMemUpBySpec);
        TurningAdvice noAdviceRes = new TurningAdvice();
        for (TurningMemUpStrategy s : strategies) {
            try {
                TurningAdvice turning = s.turning(context);
                if (turning != null && turning.getStatus() == DiagnosisTurningStatus.HAS_ADVICE) {
                    return turning;
                } else if (turning != null) {
                    noAdviceRes.setDescription(doctorUtil.addComma(noAdviceRes.getDescription()) +
                            turning.getDescription());
                }
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            }
        }
        return noAdviceRes;
    }
}
