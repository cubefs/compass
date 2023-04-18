package com.oppo.cloud.diagnosis.advice.turning.mem;


import com.oppo.cloud.diagnosis.advice.turning.TurningAdvice;
import com.oppo.cloud.diagnosis.domain.diagnosis.DiagnosisContext;

public interface TurningMemUpStrategy {
    TurningAdvice turning(DiagnosisContext context);
}
