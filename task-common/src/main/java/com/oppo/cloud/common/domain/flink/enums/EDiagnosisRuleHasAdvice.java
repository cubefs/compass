package com.oppo.cloud.common.domain.flink.enums;

import lombok.Getter;

@Getter
public enum EDiagnosisRuleHasAdvice {
    // 没有建议
    NO_ADVICE(0, "没有建议"),
    // 不适用的策略就要换下一条
    HAS_ADVICE(1, "有建议");
    private final int code;
    private final String desc;

    EDiagnosisRuleHasAdvice(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
