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

package com.oppo.cloud.portal.domain.diagnose.info;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("任务运行信息")
public class TaskInfo {

    @ApiModelProperty(value = "实例")
    private String taskName;

    @ApiModelProperty(value = "任务流")
    private String flowName;

    @ApiModelProperty(value = "项目名称")
    private String projectName;

    @ApiModelProperty(value = "执行周期")
    private String executionTime;

    @ApiModelProperty(value = "运行耗时")
    private String appTime;

    @ApiModelProperty(value = "applicationId")
    private String applicationId;

    @ApiModelProperty(value = "异常类型")
    private List<String> categories;

    @ApiModelProperty(value = "oflow链接")
    private String oflowUrl;

    @ApiModelProperty(value = "内存消耗")
    private String memorySeconds;

    @ApiModelProperty(value = "CPU消耗")
    private String vcoreSeconds;
}
