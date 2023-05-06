package com.oppo.cloud.common.domain.flink.enums;

import lombok.Getter;

/**
 * 诊断规则类型
 */
@Getter
public enum DiagnosisRuleType {
    /**
     * 资源优化类型
     */
    ResourceRule(0, "资源优化类型"),
    /**
     * 运行时异常类型
     */
    RuntimeExceptionRule(1, "运行时异常类型"),
    ;
    private final int code;
    private final String name;

    DiagnosisRuleType(int code, String name) {
        this.code = code;
        this.name = name;
    }
}
