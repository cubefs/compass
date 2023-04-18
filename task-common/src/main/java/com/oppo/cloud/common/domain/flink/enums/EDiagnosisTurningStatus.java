package com.oppo.cloud.common.domain.flink.enums;

import lombok.Getter;

@Getter
public enum EDiagnosisTurningStatus {
    // 没有建议
    NO_ADVICE(1, "没有建议"),
    // 不适用的策略就要换下一条
    NOT_SUIT(0, "不适用"),
    // 有建议的策略则可以返回
    HAS_ADVICE(2, "有建议");
    private final int code;
    private final String desc;

    EDiagnosisTurningStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
