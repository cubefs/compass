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

    @ApiModelProperty(value = "诊断任务数")
    private Integer abnormalJobNum;

    @ApiModelProperty(value = "活跃任务数")
    private Integer jobNum;

    @ApiModelProperty(value = "诊断任务数占比")
    private Double abnormalJobNumRatio;

    @ApiModelProperty(value = "诊断任务数环比")
    private Double abnormalJobNumChainRatio;

    @ApiModelProperty(value = "诊断任务数同比")
    private Double abnormalJobNumDayOnDay;

    @ApiModelProperty(value = "诊断实例数")
    private Integer abnormalJobInstanceNum;

    @ApiModelProperty(value = "运行实例数")
    private Integer jobInstanceNum;

    @ApiModelProperty(value = "诊断实例数占比")
    private Double abnormalJobInstanceNumRatio;

    @ApiModelProperty(value = "诊断实例数环比")
    private Double abnormalJobInstanceNumChainRatio;

    @ApiModelProperty(value = "诊断实例数同比")
    private Double abnormalJobInstanceNumDayOnDay;

    @ApiModelProperty(value = "任务CPU消耗数")
    private Double abnormalJobCpuNum;

    @ApiModelProperty(value = "总CPU消耗数")
    private Double jobCpuNum;

    @ApiModelProperty(value = "cpu消耗单位")
    private String cpuUnit = "vcore·s";

    @ApiModelProperty(value = "任务CPU消耗数占比")
    private Double abnormalJobCpuNumRatio;

    @ApiModelProperty(value = "任务CPU消耗数环比")
    private Double abnormalJobCpuNumChainRatio;

    @ApiModelProperty(value = "任务CPU消耗数同比")
    private Double abnormalJobCpuNumDayOnDay;

    @ApiModelProperty(value = "任务内存消耗数")
    private Double abnormalJobMemoryNum;

    @ApiModelProperty(value = "总内存消耗数")
    private Double jobMemoryNum;

    @ApiModelProperty(value = "cpu消耗单位")
    private String memoryUnit = "G·s";

    @ApiModelProperty(value = "任务CPU消耗数占比")
    private Double abnormalJobMemoryNumRatio;

    @ApiModelProperty(value = "任务CPU消耗数环比")
    private Double abnormalJobMemoryNumChainRatio;

    @ApiModelProperty(value = "任务CPU消耗数同比")
    private Double abnormalJobMemoryNumDayOnDay;
}
