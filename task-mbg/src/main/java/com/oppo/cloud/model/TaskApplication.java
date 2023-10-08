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

package com.oppo.cloud.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class TaskApplication implements Serializable {

    @ApiModelProperty(value = "Task application record id")
    private Integer id;

    @ApiModelProperty(value = "AppId(yarn application)")
    private String applicationId;

    @ApiModelProperty(value = "Task name")
    private String taskName;

    @ApiModelProperty(value = "Flow name")
    private String flowName;

    @ApiModelProperty(value = "Project name")
    private String projectName;

    @ApiModelProperty(value = "Task plan execution time")
    private Date executeTime;

    @ApiModelProperty(value = "Task retry nth time")
    private Integer retryTimes;

    @ApiModelProperty(value = "Create time")
    private Date createTime;

    @ApiModelProperty(value = "Update time")
    private Date updateTime;

    @ApiModelProperty(value = "Task scheduler log, multiple separated by commas")
    private String logPath;

    @ApiModelProperty(value = "Task type(Spark、Flink)")
    private String taskType;

    private static final long serialVersionUID = 1L;


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", applicationId=").append(applicationId);
        sb.append(", taskName=").append(taskName);
        sb.append(", flowName=").append(flowName);
        sb.append(", projectName=").append(projectName);
        sb.append(", executeTime=").append(executeTime);
        sb.append(", retryTimes=").append(retryTimes);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", logPath=").append(logPath);
        sb.append(", task_type=").append(taskType);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}
