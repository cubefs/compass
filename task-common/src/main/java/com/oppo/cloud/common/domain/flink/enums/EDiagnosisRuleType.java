package com.oppo.cloud.common.domain.flink.enums;

import lombok.Getter;

@Getter
public enum EDiagnosisRuleType {
    ResourceRule(0, "资源优化类型"),
    RuntimeExceptionRule(1, "运行时异常类型"),
    ;
    private final int code;
    private final String name;

    EDiagnosisRuleType(int code, String name) {
        this.code = code;
        this.name = name;
    }
}
