package com.oppo.cloud.common.domain.flink.enums;

import lombok.Getter;

@Getter
public enum EDiagnosisFrom {
    EveryDay(0, "每日定时诊断"),
    JobUptime(1, "作业上线诊断"),
    Manual(2, "即时诊断"),
    ;

    private final int code;
    private final String desc;

    EDiagnosisFrom(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
