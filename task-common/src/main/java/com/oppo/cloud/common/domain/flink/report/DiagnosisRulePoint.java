package com.oppo.cloud.common.domain.flink.report;

import lombok.Data;

/**
 * 诊断规则点
 */
@Data
public class DiagnosisRulePoint {
    /**
     * key
     */
    String key;
    /**
     * value
     */
    Double value;
}
