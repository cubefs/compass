/*
 * Copyright 2023 OPPO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.oppo.cloud.portal.domain.flink;

import lombok.Data;

@Data
public class DiagnosisGeneralViewNumberResp {
    private GeneralViewNumberDto generalViewNumberDto;
    private GeneralViewNumberDto generalViewNumberDtoDay1Before;
    private GeneralViewNumberDto generalViewNumberDtoDay7Before;
    /**
     * 异常作业数占比
     */
    private Float abnormalJobNumRatio = 0f;
    /**
     * 异常作业数环比占比(与上周比)
     */
    private Float abnormalJobNumChainRatio = 0f;
    /**
     * 异常作业数同比占比(与昨天比)
     */
    private Float abnormalJobNumDayOnDay = 0f;
    /**
     * 资源优化作业数占比
     */
    private Float resourceJobNumRatio = 0f;
    /**
     * 资源优化作业数环比占比(与上周比)
     */
    private Float resourceJobNumChainRatio = 0f;
    /**
     * 资源优化作业数同比占比(与昨天比)
     */
    private Float resourceJobNumDayOnDay = 0f;
    /**
     * cpu单位个
     */
    private String cpuUnit = "个";
    /**
     * 资源优化cpu数占比
     */
    private Float resourceCpuNumRatio = 0f;
    /**
     * 资源优化cpu数环比占比(与上周比)
     */
    private Float resourceCpuNumChainRatio =0f;
    /**
     * 资源优化cpu数同比占比(与昨天比)
     */
    private Float resourceCpuNumDayOnDay = 0f;
    /**
     * memory单位MB
     */
    private String memoryUnit = "MB";
    /**
     * 资源优化memory数占比
     */
    private Float resourceMemoryNumRatio = 0f;
    /**
     * 资源优化memory数环比占比(与上周比)
     */
    private Float resourceMemoryNumChainRatio=0f;
    /**
     * 资源优化memory数同比占比(与昨天比)
     */
    private Float resourceMemoryNumDayOnDay = 0f;
}
