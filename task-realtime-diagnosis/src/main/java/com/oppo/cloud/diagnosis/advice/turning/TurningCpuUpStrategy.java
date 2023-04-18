package com.oppo.cloud.diagnosis.advice.turning;

import com.oppo.cloud.diagnosis.domain.diagnosis.DiagnosisContext;

public interface TurningCpuUpStrategy {
    TurningAdvice turning(DiagnosisContext context);
}
