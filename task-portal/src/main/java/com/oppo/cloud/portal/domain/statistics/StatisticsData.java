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

package com.oppo.cloud.portal.domain.statistics;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class StatisticsData {

    @ApiModelProperty(value = "Number of abnormal jobs")
    private Integer abnormalJobNum;

    @ApiModelProperty(value = "Number of active jobs")
    private Integer jobNum;

    @ApiModelProperty(value = "Ratio of abnormal jobs")
    private Double abnormalJobNumRatio;

    @ApiModelProperty(value = "Chain ratio of abnormal jobs")
    private Double abnormalJobNumChainRatio;

    @ApiModelProperty(value = "Day On Day ratio of abnormal jobs")
    private Double abnormalJobNumDayOnDay;

    @ApiModelProperty(value = "Number of abnormal instances")
    private Integer abnormalJobInstanceNum;

    @ApiModelProperty(value = "Number of job instances")
    private Integer jobInstanceNum;

    @ApiModelProperty(value = "Ratio of abnormal instances")
    private Double abnormalJobInstanceNumRatio;

    @ApiModelProperty(value = "Chain ratio of abnormal instances")
    private Double abnormalJobInstanceNumChainRatio;

    @ApiModelProperty(value = "Day On Day ratio of abnormal instances")
    private Double abnormalJobInstanceNumDayOnDay;

    @ApiModelProperty(value = "Abnormal Job CPU number")
    private Double abnormalJobCpuNum;

    @ApiModelProperty(value = "Job CPU number")
    private Double jobCpuNum;

    @ApiModelProperty(value = "CPU unit")
    private String cpuUnit = "vcore·s";

    @ApiModelProperty(value = "Ratio of job CPU number")
    private Double abnormalJobCpuNumRatio;

    @ApiModelProperty(value = "Chain ratio of job CPU number")
    private Double abnormalJobCpuNumChainRatio;

    @ApiModelProperty(value = "Day On Day ratio of job CPU number")
    private Double abnormalJobCpuNumDayOnDay;

    @ApiModelProperty(value = "Abnormal job memory number")
    private Double abnormalJobMemoryNum;

    @ApiModelProperty(value = "Job memory number")
    private Double jobMemoryNum;

    @ApiModelProperty(value = "Memory unit")
    private String memoryUnit = "G·s";

    @ApiModelProperty(value = "Ratio of job memory number")
    private Double abnormalJobMemoryNumRatio;

    @ApiModelProperty(value = "Chain ratio of job memory number")
    private Double abnormalJobMemoryNumChainRatio;

    @ApiModelProperty(value = "Day On Day ratio of job memory number")
    private Double abnormalJobMemoryNumDayOnDay;
}
