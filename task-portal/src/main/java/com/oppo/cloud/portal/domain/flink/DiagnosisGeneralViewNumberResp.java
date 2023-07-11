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
    GeneralViewNumberDto generalViewNumberDto;
    GeneralViewNumberDto generalViewNumberDtoDay1Before;
    GeneralViewNumberDto generalViewNumberDtoDay7Before;
    /**
     * 异常作业数占比
     */
    Float abnormalJobNumRatio = 0f;
    /**
     * 异常作业数环比占比(与上周比)
     */
    Float abnormalJobNumChainRatio = 0f;
    /**
     * 异常作业数同比占比(与昨天比)
     */
    Float abnormalJobNumDayOnDay = 0f;

    /**
     * 资源优化作业数占比
     */
    Float resourceJobNumRatio = 0f;
    /**
     * 资源优化作业数环比占比(与上周比)
     */
    Float resourceJobNumChainRatio = 0f;
    /**
     * 资源优化作业数同比占比(与昨天比)
     */
    Float resourceJobNumDayOnDay = 0f;
    /**
     * cpu单位个
     */
    String cpuUnit = "个";
    /**
     * 资源优化cpu数占比
     */
    Float resourceCpuNumRatio = 0f;
    /**
     * 资源优化cpu数环比占比(与上周比)
     */
    Float resourceCpuNumChainRatio =0f;
    /**
     * 资源优化cpu数同比占比(与昨天比)
     */
    Float resourceCpuNumDayOnDay = 0f;
    /**
     * memory单位MB
     */
    String memoryUnit = "MB";
    /**
     * 资源优化memory数占比
     */
    Float resourceMemoryNumRatio = 0f;
    /**
     * 资源优化memory数环比占比(与上周比)
     */
    Float resourceMemoryNumChainRatio=0f;
    /**
     * 资源优化memory数同比占比(与昨天比)
     */
    Float resourceMemoryNumDayOnDay = 0f;
}
