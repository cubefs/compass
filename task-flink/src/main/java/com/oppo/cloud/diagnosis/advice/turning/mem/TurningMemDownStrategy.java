package com.oppo.cloud.diagnosis.advice.turning.mem;


import com.oppo.cloud.diagnosis.advice.turning.TurningAdvice;
import com.oppo.cloud.diagnosis.domain.diagnosis.DiagnosisContext;

public interface TurningMemDownStrategy {
    TurningAdvice turning(DiagnosisContext context);
}
