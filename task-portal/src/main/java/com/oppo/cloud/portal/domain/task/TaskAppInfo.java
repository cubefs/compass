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
@ApiModel(value = "Application展示结构")
@NoArgsConstructor
public class TaskAppInfo {

    @ApiModelProperty(value = "appId名称")
    private String applicationId;

    @ApiModelProperty(value = "applicationType类型")
    private String applicationType;

    @ApiModelProperty(value = "项目名称")
    private String projectName;

    @ApiModelProperty(value = "工作流名称")
    private String flowName;

    @ApiModelProperty(value = "任务名称")
    private String taskName;

    @ApiModelProperty(value = "执行周期")
    private String executionDate;

    @ApiModelProperty(value = "诊断类型")
    private List<String> categories;

    @ApiModelProperty(value = "运行耗时")
    private String duration;

    @ApiModelProperty(value = "重试次数")
    private Integer tryNumber;

    @ApiModelProperty(value = "运行消耗资源")
    private String resource;

    @ApiModelProperty(value = "创建人")
    private String users;

    @ApiModelProperty(value = "sparkUI")
    private String sparkUI;

    @ApiModelProperty(value = "任务执行状态")
    private String taskAppState;

    /**
     * 转换格式
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
        taskAppInfo.setCategories(AppCategoryEnum.getAppCategoryCh(taskApp.getCategories()));
        taskAppInfo.setUsers(taskApp.getUsers() == null ? ""
                : taskApp.getUsers().stream().map(SimpleUser::getUsername).collect(Collectors.joining(",")));
        taskAppInfo.setResource(TaskUtil.resourceSimplify(taskApp.getVcoreSeconds(), taskApp.getMemorySeconds()));
        taskAppInfo.setSparkUI(taskApp.getSparkUI());
        taskAppInfo.setTryNumber(taskApp.getRetryTimes() == null ? 0 : taskApp.getRetryTimes());
        taskAppInfo.setTaskAppState(taskApp.getTaskAppState());
        return taskAppInfo;
    }
}
