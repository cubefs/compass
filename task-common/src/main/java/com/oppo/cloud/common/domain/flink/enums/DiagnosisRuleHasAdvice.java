package com.oppo.cloud.common.domain.flink.enums;

import lombok.Getter;

/**
 * 诊断规则是否有建议
 */
@Getter
public enum DiagnosisRuleHasAdvice {
    /**
     * 无建议
     */
    NO_ADVICE(0, "没有建议"),
    /**
     * 有建议
     */
    HAS_ADVICE(1, "有建议");
    private final int code;
    private final String desc;

    DiagnosisRuleHasAdvice(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
