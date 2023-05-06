package com.oppo.cloud.common.domain.flink.enums;

/**
 * 诊断参数
 */
public enum DiagnosisParam {
    /**
     * source分区总数
     */
    PartitionSum(0, "source分区总数"),
    /**
     * CPU较低时，需要增大到cpu的目标值
     */
    CpuLowTarget(1, "CPU较低时，需要增大到cpu的目标值"),
    /**
     * CPU低的阈值
     */
    CpuLowThreshold(2, "CPU低的阈值"),
    /**
     * CPU较高时，需要减小到cpu的目标值
     */
    CpuHighTarget(3, "CPU较高时，需要减小到cpu的目标值"),
    /**
     * CPU高的阈值
     */
    CpuHighThreshold(4, "CPU高的阈值"),
    /**
     * CPU 增长change rate
     */
    GrowCpuChangeRate(5, "CPU 增长change rate"),
    /**
     * 最大流量
     */
    FlowMax(6, "最大流量"),
    /**
     * Mem较低时，需要增大到Mem的目标值
     */
    MemoryLowTarget(7, "Mem较低时，需要增大到Mem的目标值"),
    /**
     * Mem低的阈值
     */
    MemoryLowThreshold(8, "Mem低的阈值"),
    /**
     * Mem较高时，需要减小到Mem的目标值
     */
    MemoryHighTarget(9, "Mem较高时，需要减小到Mem的目标值"),
    /**
     * Mem高的阈值
     */
    MemoryHighThreshold(10, "Mem高的阈值"),
    /**
     * flink 的jm 的id
     */
    JobId(11, "flink 的jm 的id"),
    /**
     * flink 的tm 的id
     */
    TmIds(12, "flink 的tm 的id"),
    /**
     * flink 上报prometheus的job
     */
    Job(13, "flink 上报prometheus的job"),
    ;

    private final int code;
    private final String desc;

    DiagnosisParam(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
