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
     * Ratio of the abnormal job number
     */
    private Float abnormalJobNumRatio = 0f;
    /**
     * Chain ratio of the abnormal job number(compared to last week)
     */
    private Float abnormalJobNumChainRatio = 0f;
    /**
     * Abnormal job number day on day(compared to yesterday)
     */
    private Float abnormalJobNumDayOnDay = 0f;
    /**
     * Ratio of the resource job number
     */
    private Float resourceJobNumRatio = 0f;
    /**
     * Chain ratio of the resource job number(compared to last week)
     */
    private Float resourceJobNumChainRatio = 0f;
    /**
     * Resource job number day on day(compared to yesterday)
     */
    private Float resourceJobNumDayOnDay = 0f;
    /**
     * CPU unit
     */
    private String cpuUnit = "个";
    /**
     * Ratio of the resource cpu number
     */
    private Float resourceCpuNumRatio = 0f;
    /**
     * Chain ratio of the resource cpu number(compared to last week)
     */
    private Float resourceCpuNumChainRatio =0f;
    /**
     * Resource cpu number day on day(compared to yesterday)
     */
    private Float resourceCpuNumDayOnDay = 0f;
    /**
     * Memory unit
     */
    private String memoryUnit = "MB";
    /**
     * Ratio of the resource memory number
     */
    private Float resourceMemoryNumRatio = 0f;
    /**
     * Chain ratio of the resource memory number(compared to last week)
     */
    private Float resourceMemoryNumChainRatio=0f;
    /**
     * Resource memory number day on day(compared to yesterday)
     */
    private Float resourceMemoryNumDayOnDay = 0f;
}
