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

package com.oppo.cloud.portal.domain.task;

import com.oppo.cloud.common.constant.AppCategoryEnum;
import com.oppo.cloud.common.domain.opensearch.SimpleUser;
import com.oppo.cloud.common.domain.opensearch.TaskApp;
import com.oppo.cloud.common.util.DateUtil;
import com.oppo.cloud.portal.util.TaskUtil;
import com.oppo.cloud.portal.util.UnitUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@ApiModel(value = "Application information")
@NoArgsConstructor
public class TaskAppInfo {

    @ApiModelProperty(value = "appId")
    private String applicationId;

    @ApiModelProperty(value = "application type")
    private String applicationType;

    @ApiModelProperty(value = "project name")
    private String projectName;

    @ApiModelProperty(value = "flow name")
    private String flowName;

    @ApiModelProperty(value = "task name")
    private String taskName;

    @ApiModelProperty(value = "execution date")
    private String executionDate;

    @ApiModelProperty(value = "categories")
    private List<String> categories;

    @ApiModelProperty(value = "duration")
    private String duration;

    @ApiModelProperty(value = "try number")
    private Integer tryNumber;

    @ApiModelProperty(value = "resource")
    private String resource;

    @ApiModelProperty(value = "users")
    private String users;

    @ApiModelProperty(value = "sparkUI")
    private String sparkUI;

    @ApiModelProperty(value = "task app state")
    private String taskAppState;

    /**
     * format TaskApp
     */
    public static TaskAppInfo from(TaskApp taskApp) {
        TaskAppInfo taskAppInfo = new TaskAppInfo();
        taskAppInfo.setApplicationId(taskApp.getApplicationId());
        taskAppInfo.setApplicationType(taskApp.getApplicationType());
        taskAppInfo.setProjectName(taskApp.getProjectName());
        taskAppInfo.setFlowName(taskApp.getFlowName());
        taskAppInfo.setTaskName(taskApp.getTaskName());
        taskAppInfo.setExecutionDate(DateUtil.format(taskApp.getExecutionDate()));
        taskAppInfo.setDuration(UnitUtil.transferSecond(taskApp.getElapsedTime() / 1000));
        taskAppInfo.setCategories(AppCategoryEnum.getLangMsgByCategories(taskApp.getCategories()));
        taskAppInfo.setUsers(taskApp.getUsers() == null ? ""
                : taskApp.getUsers().stream().map(SimpleUser::getUsername).collect(Collectors.joining(",")));
        taskAppInfo.setResource(TaskUtil.resourceSimplify(taskApp.getVcoreSeconds(), taskApp.getMemorySeconds()));
        taskAppInfo.setSparkUI(taskApp.getSparkUI());
        taskAppInfo.setTryNumber(taskApp.getRetryTimes() == null ? 0 : taskApp.getRetryTimes());
        taskAppInfo.setTaskAppState(taskApp.getTaskAppState());
        return taskAppInfo;
    }
}
