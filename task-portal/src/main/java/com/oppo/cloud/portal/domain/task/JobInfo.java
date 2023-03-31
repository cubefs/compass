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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.oppo.cloud.common.constant.AppCategoryEnum;
import com.oppo.cloud.common.constant.JobCategoryEnum;
import com.oppo.cloud.common.domain.elasticsearch.JobAnalysis;
import com.oppo.cloud.common.domain.elasticsearch.SimpleUser;
import com.oppo.cloud.common.util.DateUtil;
import com.oppo.cloud.portal.util.TaskUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@ApiModel(value = "Job展示结构")
@NoArgsConstructor
public class JobInfo {

    @JsonIgnore
    private String Id;

    @ApiModelProperty(value = "用户名称")
    private String users;

    @ApiModelProperty(value = "项目名称")
    private String projectName;

    @ApiModelProperty(value = "工作流")
    private String flowName;

    @ApiModelProperty(value = "任务名称")
    private String taskName;

    @ApiModelProperty(value = "执行周期")
    private String executionDate;

    @ApiModelProperty(value = "开始时间")
    private String startTime;

    @ApiModelProperty(value = "结束时间")
    private String endTime;

    @ApiModelProperty(value = "任务执行耗时")
    private String duration;

    @ApiModelProperty(value = "任务执行状态")
    private String taskState;

    @ApiModelProperty(value = "诊断类型")
    private List<String> categories;

    @ApiModelProperty(value = "资源消耗")
    private String resource;

    @ApiModelProperty(value = "重试次数")
    private Integer tryNumber;

    @ApiModelProperty(value = "其他信息")
    private List<String> Others;

    @ApiModelProperty(value = "任务处理状态：未处理(0)、已处理(1)")
    private Integer taskStatus;

    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @ApiModelProperty(value = "更新时间")
    private String updateTime;

    public static JobInfo from(JobAnalysis jobAnalysis, Object stateCache) {
        JobInfo jobInfo = new JobInfo();
        jobInfo.setProjectName(jobAnalysis.getProjectName());
        jobInfo.setFlowName(jobAnalysis.getFlowName());
        jobInfo.setTaskName(jobAnalysis.getTaskName());
        jobInfo.setTryNumber(jobAnalysis.getRetryTimes());
        jobInfo.setUsers(jobAnalysis.getUsers() == null ? ""
                : jobAnalysis.getUsers().stream().map(SimpleUser::getUsername).collect(Collectors.joining(",")));
        jobInfo.setExecutionDate(
                jobAnalysis.getExecutionDate() == null ? "" : DateUtil.format(jobAnalysis.getExecutionDate()));
        jobInfo.setDuration(jobAnalysis.getStartTime() != null && jobAnalysis.getEndTime() != null
                ? DateUtil.timeSimplify(jobAnalysis.getStartTime(), jobAnalysis.getEndTime())
                : "");
        jobInfo.setStartTime(jobAnalysis.getStartTime() != null ? DateUtil.format(jobAnalysis.getStartTime()) : "");
        jobInfo.setEndTime(jobAnalysis.getEndTime() != null ? DateUtil.format(jobAnalysis.getEndTime()) : "");
        jobInfo.setCreateTime(jobAnalysis.getCreateTime() != null ? DateUtil.format(jobAnalysis.getCreateTime()) : "");
        jobInfo.setUpdateTime(jobAnalysis.getUpdateTime() != null ? DateUtil.format(jobAnalysis.getUpdateTime()) : "");
        List<String> categories = new ArrayList<>();
        categories.addAll(JobCategoryEnum.getJobCategoryCh(jobAnalysis.getCategories()));
        categories.addAll(AppCategoryEnum.getAppCategoryCh(jobAnalysis.getCategories()));
        jobInfo.setCategories(categories);
        jobInfo.setTaskStatus(jobAnalysis.getTaskStatus() == null ? 0 : jobAnalysis.getTaskStatus());
        if (stateCache != null) {
            long executionDate = Long.parseLong((String) stateCache);
            if (executionDate >= jobAnalysis.getExecutionDate().getTime()) {
                jobInfo.setTaskStatus(1);
            }
        }
        jobInfo.setTaskState(jobAnalysis.getTaskState());

        jobInfo.setResource(TaskUtil.resourceSimplify(jobAnalysis.getVcoreSeconds(), jobAnalysis.getMemorySeconds()));

        List<String> others = new ArrayList<>();

        if (jobAnalysis.getMemory() != null) {
            others.add(String.format("广播使用内存%f，占比内存%f", jobAnalysis.getMemory(), jobAnalysis.getMemoryRatio()));
        }
        if (jobAnalysis.getDurationBaseline() != null) {
            others.add(String.format("正常耗时区间:0 ~ %s", jobAnalysis.getDurationBaseline()));
        }
        if (jobAnalysis.getEndTimeBaseline() != null) {
            others.add(String.format("正常结束区间:<%s", jobAnalysis.getEndTimeBaseline()));
        }
        if (jobAnalysis.getSuccessExecutionDay() != null && jobAnalysis.getSuccessDays() != null) {
            others.add(String.format("最近一次成功的时间:%s，距今%s天", jobAnalysis.getSuccessExecutionDay(),
                    jobAnalysis.getSuccessDays()));
        }
        // TODO: 其他
        jobInfo.setOthers(others);
        return jobInfo;
    }
}
