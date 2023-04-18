package com.oppo.cloud.diagnosis.advice;

import com.oppo.cloud.diagnosis.domain.diagnosis.DiagnosisContext;
import com.oppo.cloud.diagnosis.domain.diagnosis.RcJobDiagnosisAdvice;

/**
 * 诊断规则接口
 */
public interface IAdviceRule {
    RcJobDiagnosisAdvice advice(DiagnosisContext r);
}
