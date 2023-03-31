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

package com.oppo.cloud.common.domain.gc;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;


@Data
@ApiModel("内存分析")
public class MemoryAnalyze {

    /**
     * appId
     */
    @ApiModelProperty(value = "appId")
    private String applicationId;
    /**
     * oflow task id
     */
    @ApiModelProperty(value = "oflow task id")
    private String oflowTaskId;
    /**
     * oflow dag id
     */
    @ApiModelProperty(value = "oflow dag id")
    private String dagId;
    /**
     * execution date
     */
    @ApiModelProperty(value = "execution date")
    private Integer executionDate;
    /**
     * 区域
     */
    @ApiModelProperty(value = "区域")
    private String zone;
    /**
     * 日志类型 driver executor
     */
    @ApiModelProperty(value = "日志类型 driver executor")
    private String logType;

    /**
     * executor的内存使用
     */
    @ApiModelProperty(value = "executor的内存使用")
    private List<ExecutorPeakMemory> executorPeakMemoryList;
}
