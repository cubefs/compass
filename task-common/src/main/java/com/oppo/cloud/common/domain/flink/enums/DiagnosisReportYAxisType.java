package com.oppo.cloud.common.domain.flink.enums;

/**
 * 诊断报告Y轴类型
 */
public enum DiagnosisReportYAxisType {
    /**
     * 百分比
     */
    Percent(0, "percent 0-1"),
    /**
     * 数值
     */
    Numeric(1, "数值"),
    /**
     * 时间秒
     */
    Second(2, "时间秒"),
    /**
     * 速度bytes/s
     */
    BytesPerSecond(3, "byte/s"),
    ;

    private final int code;
    private final String desc;

    DiagnosisReportYAxisType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
