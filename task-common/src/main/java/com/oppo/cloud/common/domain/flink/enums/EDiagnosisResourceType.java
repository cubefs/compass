package com.oppo.cloud.common.domain.flink.enums;

import lombok.Getter;

@Getter
public enum EDiagnosisResourceType {
    // 扩容cpu
    GROW_CPU(0, "扩容cpu"),
    // 扩容内存
    GROW_MEM(1, "扩容内存"),
    // 缩减cpu
    CUT_CPU(2, "缩减cpu"),
    // 缩减内存
    CUT_MEM(3, "缩减内存"),
    EXCEPTION(4, "运行异常"),
    ;
    private final int code;
    private final String desc;

    EDiagnosisResourceType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
