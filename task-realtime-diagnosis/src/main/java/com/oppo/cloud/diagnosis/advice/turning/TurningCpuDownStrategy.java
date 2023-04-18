package com.oppo.cloud.diagnosis.advice.turning;

import com.oppo.cloud.diagnosis.domain.diagnosis.DiagnosisContext;

public interface TurningCpuDownStrategy {
    TurningAdvice turning(DiagnosisContext context);
}
