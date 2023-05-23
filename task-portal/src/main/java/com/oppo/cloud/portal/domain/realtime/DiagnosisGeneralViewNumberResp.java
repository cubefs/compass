package com.oppo.cloud.portal.domain.realtime;

import lombok.Data;

@Data
public class DiagnosisGeneralViewNumberResp {
    GeneralViewNumberDto generalViewNumberDto;
    GeneralViewNumberDto generalViewNumberDtoDay1Before;
    GeneralViewNumberDto generalViewNumberDtoDay7Before;
    /**
     * 异常作业数占比
     */
    Float abnormalJobNumRatio;
    /**
     * 异常作业数环比占比(与上周比)
     */
    Float abnormalJobNumChainRatio;
    /**
     * 异常作业数同比占比(与昨天比)
     */
    Float abnormalJobNumDayOnDay;

    /**
     * 资源优化作业数占比
     */
    Float resourceJobNumRatio;
    /**
     * 资源优化作业数环比占比(与上周比)
     */
    Float resourceJobNumChainRatio;
    /**
     * 资源优化作业数同比占比(与昨天比)
     */
    Float resourceJobNumDayOnDay;
    /**
     * cpu单位个
     */
    String cpuUnit = "个";
    /**
     * 资源优化cpu数占比
     */
    Float resourceCpuNumRatio;
    /**
     * 资源优化cpu数环比占比(与上周比)
     */
    Float resourceCpuNumChainRatio;
    /**
     * 资源优化cpu数同比占比(与昨天比)
     */
    Float resourceCpuNumDayOnDay;
    /**
     * memory单位MB
     */
    String memoryUnit = "MB";
    /**
     * 资源优化memory数占比
     */
    Float resourceMemoryNumRatio;
    /**
     * 资源优化memory数环比占比(与上周比)
     */
    Float resourceMemoryNumChainRatio;
    /**
     * 资源优化memory数同比占比(与昨天比)
     */
    Float resourceMemoryNumDayOnDay;
}
