package com.oppo.cloud.common.domain.flink.enums;

import lombok.Getter;

@Getter
public enum DiagnosisFrom {
    /**
     * 每日定时诊断
     */
    EveryDay(0, "每日定时诊断"),
    /**
     * 作业上线诊断
     */
    JobUptime(1, "作业上线诊断"),
    /**
     * 即时诊断
     */
    Manual(2, "即时诊断"),
    ;

    private final int code;
    private final String desc;

    DiagnosisFrom(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
