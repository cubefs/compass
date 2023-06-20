package com.oppo.cloud.diagnosis.domain.diagnosis;


import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 诊断实体
 */
@Data
@Accessors(chain = true)
public class RcJobDiagnosis {

    private static final long serialVersionUID = 1L;

    /**
     * promethues上报的作业名称
     */
    private String jobName;

    /**
     * 并行度
     */
    private Integer parallel;

    /**
     * tm slot数量
     */
    private Integer tmSlotNum;

    /**
     * tm 个数
     */
    private Integer tmNum;
    /**
     * tm memory
     */
    private Integer tmMem;
    /**
     * jm memory
     */
    private Integer jmMem;
    /**
     * tm core
     */
    private Integer tmCore;
    /**
     * kafka partition number
     */
    private Integer kafkaConsumePartitionNum;


    /**
     * 建议并行度
     */
    private Integer diagnosisParallel;

    /**
     * 建议tm的slot数量
     */
    private Integer diagnosisTmSlot;

    /**
     * 建议的tm个数
     */
    private Integer diagnosisTmNum;

    /**
     * 建议tm的slot数量
     */
    private Integer diagnosisTmCore;

    /**
     * 建议tm的memory MB
     */
    private Integer diagnosisTmMem;

    /**
     * 建议jm的memory MB
     */
    private Integer diagnosisJmMem;

    /**
     * tm整体平均 cpu使用率最大值
     */
    private Float tmAvgCpuUsageMax;

    /**
     * tm整体平均 cpu使用率最小值
     */
    private Float tmAvgCpuUsageMin;

    /**
     * tm整体平均 cpu使用率平均值
     */
    private Float tmAvgCpuUsageAvg;

}
