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

import com.oppo.cloud.common.domain.opensearch.FlinkTaskAdvice;
import com.oppo.cloud.common.domain.opensearch.FlinkTaskAnalysis;
import com.oppo.cloud.common.domain.opensearch.SimpleUser;
import com.oppo.cloud.common.util.DateUtil;
import com.oppo.cloud.portal.util.UnitUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@ApiModel(value = "flink task web information")
public class FlinkTaskAnalysisInfo {

    @ApiModelProperty(value = "id")
    private String id;

    @ApiModelProperty(value = "yarn applicationId")
    private String applicationId;

    @ApiModelProperty(value = "project name")
    private String projectName;

    @ApiModelProperty(value = "flow name")
    private String flowName;

    @ApiModelProperty(value = "task name")
    private String taskName;

    @ApiModelProperty(value = "job name")
    private String jobName;

    @ApiModelProperty(value = "start time")
    private String startTime;

    @ApiModelProperty(value = "time cost")
    private String timeCost;

    @ApiModelProperty(value = "queue")
    private String queue;

    @ApiModelProperty(value = "parallel")
    private Integer parallel;

    /* flink slot */
    private Integer tmSlot;

    /* flink task manager core */
    private Integer tmCore;

    /* flink task manager memory */
    private Integer tmMemory;

    /* flink job manager memory */
    private Integer jmMemory;


    /* flink task manager num */
    private Integer tmNum;

    @ApiModelProperty(value = "resource cost")
    private String resourceCost;

    @ApiModelProperty(value = "username")
    private String username;

    @ApiModelProperty(value = "mem cost")
    private String memCost;

    @ApiModelProperty(value = "rule names")
    private List<String> ruleNames;

    @ApiModelProperty(value = "flink track url")
    private String flinkTrackUrl;

    @ApiModelProperty(value = "resource advice")
    private String resourceAdvice;

    @ApiModelProperty(value = "diagnosis start time")
    private String diagnosisStartTime;

    @ApiModelProperty(value = "diagnosis end time")
    private String diagnosisEndTime;

    /* diagnosis parallel */
    private Integer diagnosisParallel;

    /* diagnosis job manager memory unit: MB */
    private Integer diagnosisJmMemory;

    /* diagnosis task manager memory unit: MB */
    private Integer diagnosisTmMemory;

    /* diagnosis tm slot number */
    private Integer diagnosisTmSlotNum;

    /* diagnosis tm core number */
    private Integer diagnosisTmCoreNum;

    @ApiModelProperty(value = "create time")
    private String createTime;

    @ApiModelProperty(value = "update time")
    private String updateTime;

    /**
     * format FlinkTaskAnalysis
     *
     * @param item
     * @return
     */
    public static FlinkTaskAnalysisInfo from(FlinkTaskAnalysis item) {
        FlinkTaskAnalysisInfo info = new FlinkTaskAnalysisInfo();
        // basic info
        info.setId(item.getDocId());
        info.setApplicationId(item.getApplicationId());
        info.setProjectName(item.getProjectName());
        info.setFlowName(item.getFlowName());
        info.setTaskName(item.getTaskName());
        info.setJobName(item.getJobName());
        info.setQueue(item.getQueue());
        info.setParallel(item.getParallel());
        info.setFlinkTrackUrl(item.getFlinkTrackUrl());
        info.setTmCore(item.getTmCore());
        info.setTmMemory(item.getTmMemory());
        info.setJmMemory(item.getJmMemory());
        info.setTmSlot(item.getTmSlot());
        info.setTmNum(item.getTmNum());
        info.setRuleNames(item.getDiagnosisTypes());
        info.setCreateTime(DateUtil.format(item.getCreateTime()));
        info.setUpdateTime(DateUtil.format(item.getUpdateTime()));
        info.setUsername(item.getUsers().stream().map(SimpleUser::getUsername).collect(Collectors.joining(",")));
        info.setStartTime(DateUtil.format(item.getStartTime()));
        info.setDiagnosisStartTime(DateUtil.format(item.getDiagnosisStartTime()));
        info.setDiagnosisEndTime(DateUtil.format(item.getDiagnosisEndTime()));
        info.setDiagnosisJmMemory(item.getDiagnosisJmMemory());
        info.setDiagnosisTmMemory(item.getDiagnosisTmMemory());
        info.setDiagnosisParallel(item.getDiagnosisParallel());
        info.setDiagnosisTmCoreNum(item.getDiagnosisTmCoreNum());
        info.setDiagnosisTmSlotNum(item.getDiagnosisTmSlotNum());

        // cost
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = LocalDateTime.ofEpochSecond(item.getStartTime().getTime() / 1000, 0, ZoneOffset.ofHours(8));

        Duration duration = Duration.between(start, now);
        String timeCost = UnitUtil.transferTimeUnit(duration.getSeconds() * 1000);
        String vcoreCosts = UnitUtil.transferVcoreS(duration.getSeconds() * ((long) item.getTmCore() * item.getTmNum() + 1));
        String memCost = UnitUtil.transferMemGbS(duration.getSeconds() * ((long) (float) item.getTmMemory() / 1024 * item.getTmNum() + 1));

        info.setTimeCost(timeCost);
        info.setResourceCost(vcoreCosts);
        info.setMemCost(memCost);
        // advice
        List<FlinkTaskAdvice> flinkTaskAdvices = item.getAdvices();
        List<String> advices = new ArrayList<>();
        for (FlinkTaskAdvice flinkTaskAdvice : flinkTaskAdvices) {
            if (flinkTaskAdvice.getHasAdvice() == 1) { // has advice == 1
                advices.add(flinkTaskAdvice.getDescription());
            }
        }

        if (!item.getDiagnosisParallel().equals(item.getParallel())) {
            advices.add(String.format("作业并行度:%d->%d", item.getParallel(), item.getDiagnosisParallel()));
        }
        if (!item.getDiagnosisTmSlotNum().equals(item.getTmSlot())) {
            advices.add(String.format("作业TM的Slot数:%d->%d", item.getTmSlot(), item.getDiagnosisTmSlotNum()));
        }
        if (!item.getDiagnosisTmCoreNum().equals(item.getTmCore())) {
            advices.add(String.format("作业TM的Core数:%d->%d", item.getTmCore(), item.getDiagnosisTmCoreNum()));
        }
        if (!item.getDiagnosisTmMemory().equals(item.getTmMemory())) {
            advices.add(String.format("作业TM的内存:%dMB->%dMB", item.getTmMemory(), item.getDiagnosisTmMemory()));
        }
        if (!item.getDiagnosisJmMemory().equals(item.getJmMemory())) {
            advices.add(String.format("作业JM的内存:%dMB->%dMB", item.getJmMemory(), item.getDiagnosisJmMemory()));
        }
        info.setResourceAdvice(String.join(";", advices));
        return info;
    }
}
