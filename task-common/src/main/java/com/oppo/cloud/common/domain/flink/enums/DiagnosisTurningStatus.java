package com.oppo.cloud.common.domain.flink.enums;

import lombok.Getter;

/**
 * 诊断调优状态
 */
@Getter
public enum DiagnosisTurningStatus {
    /**
     * 无建议
     */
    NO_ADVICE(1, "没有建议"),
    /**
     * 不适用
     */
    NOT_SUIT(0, "不适用"),
    /**
     * 有建议
     */
    HAS_ADVICE(2, "有建议");
    private final int code;
    private final String desc;

    DiagnosisTurningStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
