package com.oppo.cloud.common.domain.flink.enums;

public enum EDiagnosisReportYAxisType {
    Percent(0, "percent 0-1"),
    Numeric(1, "数值"),
    Second(2, "时间秒"),
    BytesPerSecond(3, "byte/s"),
    ;

    private final int code;
    private final String desc;

    EDiagnosisReportYAxisType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
