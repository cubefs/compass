package com.oppo.cloud.portal.domain.statistics;

import lombok.Data;

/**
 * Flink作业概览数据统计
 */
@Data
public class FlinkStatisticsData {
    /*诊断作业数*/
    private long jobCount;
    /*异常作业数*/
    private long exceptionJobCount;
    /*异常作业占比*/
    private double exceptionJobRatio;
    /*可优化资源作业数*/
    private long resourceJobCount;
    /*可优化资源作业占比*/
    private double resourceJobRatio;
    /*可优化CPU数*/
    private double decrCPUCount;
    /*总CPU数*/
    private double totalCPUCount;
    /*可优化CPU占比*/
    private double decrCPURatio;
    /*可优化内存数*/
    private double decrMemory;
    /*总内存*/
    private double totalMemory;
    /*可优化内存占比*/
    private double decrMemoryRatio;
}
