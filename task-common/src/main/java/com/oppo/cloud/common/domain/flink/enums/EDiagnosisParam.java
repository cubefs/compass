package com.oppo.cloud.common.domain.flink.enums;

public enum EDiagnosisParam {
    PartitionSum(0, "source分区总数"),
    CpuLowTarget(1, "CPU较低时，需要增大到cpu的目标值"),
    CpuLowThreshold(2, "CPU低的阈值"),
    CpuHighTarget(3, "CPU较高时，需要减小到cpu的目标值"),
    CpuHighThreshold(4, "CPU高的阈值"),
    GrowCpuChangeRate(5, "cpu 增长change rate"),
    FlowMax(6, "最大流量"),
    MemLowTarget(7, "Mem较低时，需要增大到Mem的目标值"),
    MemLowThreshold(8, "Mem低的阈值"),
    MemHighTarget(9, "Mem较高时，需要减小到Mem的目标值"),
    MemHighThreshold(10, "Mem高的阈值"),
    JobId(11, "flink 的jm 的id"),
    TmIds(12, "flink 的tm 的id"),
    Job(13, "flink 上报prometheus的job"),
    ;

    private final int code;
    private final String desc;

    EDiagnosisParam(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
