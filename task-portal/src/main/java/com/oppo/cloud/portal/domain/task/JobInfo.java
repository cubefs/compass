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
import com.oppo.cloud.common.domain.opensearch.JobAnalysis;
import com.oppo.cloud.common.domain.opensearch.SimpleUser;
import com.oppo.cloud.common.util.DateUtil;
import com.oppo.cloud.portal.util.MessageSourceUtil;
import com.oppo.cloud.portal.util.TaskUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@ApiModel(value = "Job information")
@NoArgsConstructor
public class JobInfo {

    @JsonIgnore
    private String Id;

    @ApiModelProperty(value = "users")
    private String users;

    @ApiModelProperty(value = "project name")
    private String projectName;

    @ApiModelProperty(value = "flow name")
    private String flowName;

    @ApiModelProperty(value = "task name")
    private String taskName;

    @ApiModelProperty(value = "execution date")
    private String executionDate;

    @ApiModelProperty(value = "start time")
    private String startTime;

    @ApiModelProperty(value = "end time")
    private String endTime;

    @ApiModelProperty(value = "duration")
    private String duration;

    @ApiModelProperty(value = "task state")
    private String taskState;

    @ApiModelProperty(value = "categories")
    private List<String> categories;

    @ApiModelProperty(value = "resource")
    private String resource;

    @ApiModelProperty(value = "try number")
    private Integer tryNumber;

    @ApiModelProperty(value = "Others")
    private List<String> Others;

    @ApiModelProperty(value = "task processing status: unprocessed (0), processed (1)")
    private Integer taskStatus;

    @ApiModelProperty(value = "create time")
    private String createTime;

    @ApiModelProperty(value = "update time")
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
        categories.addAll(JobCategoryEnum.getLangMsgByCategories(jobAnalysis.getCategories()));
        categories.addAll(AppCategoryEnum.getLangMsgByCategories(jobAnalysis.getCategories()));
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
            others.add(String.format("%s%f, %s%f", MessageSourceUtil.get("BROADCAST_USED_MEMORY"), jobAnalysis.getMemory(),
                    MessageSourceUtil.get("RATIO_MEMORY"), jobAnalysis.getMemoryRatio()));
        }
        if (jobAnalysis.getDurationBaseline() != null) {
            others.add(String.format("%s:0 ~ %s", MessageSourceUtil.get("BASELINE_DURATION"), jobAnalysis.getDurationBaseline()));
        }
        if (jobAnalysis.getEndTimeBaseline() != null) {
            others.add(String.format("%s:<%s", MessageSourceUtil.get("BASELINE_END_TIME"), jobAnalysis.getEndTimeBaseline()));
        }
        if (jobAnalysis.getSuccessExecutionDay() != null && jobAnalysis.getSuccessDays() != null) {
            others.add(String.format("%s:%s, %s%s%s", MessageSourceUtil.get("LAST_SUCCESSFUL_TIME"), jobAnalysis.getSuccessExecutionDay(),
                    MessageSourceUtil.get("SINCE_NOW"), jobAnalysis.getSuccessDays(), MessageSourceUtil.get("DAY")));
        }
        // TODO: 其他
        jobInfo.setOthers(others);
        return jobInfo;
    }
}
