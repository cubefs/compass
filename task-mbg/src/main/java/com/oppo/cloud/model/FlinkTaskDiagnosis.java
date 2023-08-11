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
import java.util.List;

@Data
public class FlinkTaskDiagnosis implements Serializable {

    @ApiModelProperty(value = "实时任务诊断结果id")
    private String id;

    private Integer flinkTaskAppId;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "用户记录id")
    private Integer userId;

    @ApiModelProperty(value = "项目名")
    private String projectName;

    @ApiModelProperty(value = "项目id")
    private Integer projectId;

    @ApiModelProperty(value = "工作流名称")
    private String flowName;

    @ApiModelProperty(value = "工作流id")
    private Integer flowId;

    @ApiModelProperty(value = "任务名称")
    private String taskName;

    @ApiModelProperty(value = "任务id")
    private Integer taskId;

    @ApiModelProperty(value = "appId")
    private String applicationId;

    @ApiModelProperty(value = "flink track url")
    private String flinkTrackUrl;

    @ApiModelProperty(value = "yarn获取的总共分配mb")
    private Integer allocatedMb;

    @ApiModelProperty(value = "yarn获取的总共分配vcore")
    private Integer allocatedVcores;

    @ApiModelProperty(value = "yarn获取的总共分配容器")
    private Integer runningContainers;

    @ApiModelProperty(value = "执行引擎")
    private String engineType;

    @ApiModelProperty(value = "执行周期")
    private Date executionTime;

    @ApiModelProperty(value = "运行耗时")
    private Double duration;

    @ApiModelProperty(value = "开始时间")
    private Date startTime;

    @ApiModelProperty(value = "结束时间")
    private Date endTime;

    @ApiModelProperty(value = "cpu消耗(vcore-seconds)")
    private Float vcoreSeconds;

    @ApiModelProperty(value = "内存消耗(GB-seconds)")
    private Float memorySeconds;

    @ApiModelProperty(value = "运行队列")
    private String queue;

    @ApiModelProperty(value = "集群名称")
    private String clusterName;

    @ApiModelProperty(value = "重试次数")
    private Integer retryTimes;

    @ApiModelProperty(value = "执行用户")
    private String executeUser;

    @ApiModelProperty(value = "yarn诊断信息")
    private String diagnosis;

    @ApiModelProperty(value = "flink 并行度")
    private Integer parallel;

    @ApiModelProperty(value = "flink slot")
    private Integer tmSlot;

    @ApiModelProperty(value = "flink tm core")
    private Integer tmCore;

    @ApiModelProperty(value = "flink tm_mem")
    private Integer tmMem;

    @ApiModelProperty(value = "flink jm_mem")
    private Integer jmMem;

    @ApiModelProperty(value = "flink tm_num")
    private Integer tmNum;

    @ApiModelProperty(value = "job name")
    private String jobName;

    @ApiModelProperty(value = "诊断结束时间")
    private Date diagnosisEndTime;

    @ApiModelProperty(value = "诊断起始时间")
    private Date diagnosisStartTime;

    @ApiModelProperty(value = "资源诊断类型,[0扩容cpu,1扩容mem,2缩减cpu,3缩减mem,4运行异常]")
    private String diagnosisResourceType;

    @ApiModelProperty(value = "诊断来源0凌晨定时任务,1任务上线后诊断,2即时诊断")
    private Integer diagnosisFrom;

    @ApiModelProperty(value = "建议并行度")
    private Integer diagnosisParallel;

    @ApiModelProperty(value = "建议job manager 内存大小单位MB")
    private Integer diagnosisJmMemSize;

    @ApiModelProperty(value = "建议task manager 内存大小单位MB")
    private Integer diagnosisTmMemSize;

    @ApiModelProperty(value = "建议tm的slot数量")
    private Integer diagnosisTmSlotNum;

    @ApiModelProperty(value = "建议tm的core数量")
    private Integer diagnosisTmCoreNum;

    @ApiModelProperty(value = "建议tm数量")
    private Integer diagnosisTmNum;

    @ApiModelProperty(value = "诊断问题类型拼接字符串[0][1][2][3]")
    private String diagnosisTypes;

    @ApiModelProperty(value = "处理状态(processing, success, failed)")
    private String processState;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
    String timeCost;
    String resourceCost;
    String memCost;
    String resourceAdvice;
    List<String> ruleNames;
    private static final long serialVersionUID = 1L;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getFlinkTaskAppId() {
        return flinkTaskAppId;
    }

    public void setFlinkTaskAppId(Integer flinkTaskAppId) {
        this.flinkTaskAppId = flinkTaskAppId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public Integer getFlowId() {
        return flowId;
    }

    public void setFlowId(Integer flowId) {
        this.flowId = flowId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getFlinkTrackUrl() {
        return flinkTrackUrl;
    }

    public void setFlinkTrackUrl(String flinkTrackUrl) {
        this.flinkTrackUrl = flinkTrackUrl;
    }

    public Integer getAllocatedMb() {
        return allocatedMb;
    }

    public void setAllocatedMb(Integer allocatedMb) {
        this.allocatedMb = allocatedMb;
    }

    public Integer getAllocatedVcores() {
        return allocatedVcores;
    }

    public void setAllocatedVcores(Integer allocatedVcores) {
        this.allocatedVcores = allocatedVcores;
    }

    public Integer getRunningContainers() {
        return runningContainers;
    }

    public void setRunningContainers(Integer runningContainers) {
        this.runningContainers = runningContainers;
    }

    public String getEngineType() {
        return engineType;
    }

    public void setEngineType(String engineType) {
        this.engineType = engineType;
    }

    public Date getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Date executionTime) {
        this.executionTime = executionTime;
    }

    public Double getDuration() {
        return duration;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Float getVcoreSeconds() {
        return vcoreSeconds;
    }

    public void setVcoreSeconds(Float vcoreSeconds) {
        this.vcoreSeconds = vcoreSeconds;
    }

    public Float getMemorySeconds() {
        return memorySeconds;
    }

    public void setMemorySeconds(Float memorySeconds) {
        this.memorySeconds = memorySeconds;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Integer getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(Integer retryTimes) {
        this.retryTimes = retryTimes;
    }

    public String getExecuteUser() {
        return executeUser;
    }

    public void setExecuteUser(String executeUser) {
        this.executeUser = executeUser;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public Integer getParallel() {
        return parallel;
    }

    public void setParallel(Integer parallel) {
        this.parallel = parallel;
    }

    public Integer getTmSlot() {
        return tmSlot;
    }

    public void setTmSlot(Integer tmSlot) {
        this.tmSlot = tmSlot;
    }

    public Integer getTmCore() {
        return tmCore;
    }

    public void setTmCore(Integer tmCore) {
        this.tmCore = tmCore;
    }

    public Integer getTmMem() {
        return tmMem;
    }

    public void setTmMem(Integer tmMem) {
        this.tmMem = tmMem;
    }

    public Integer getJmMem() {
        return jmMem;
    }

    public void setJmMem(Integer jmMem) {
        this.jmMem = jmMem;
    }

    public Integer getTmNum() {
        return tmNum;
    }

    public void setTmNum(Integer tmNum) {
        this.tmNum = tmNum;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public Date getDiagnosisEndTime() {
        return diagnosisEndTime;
    }

    public void setDiagnosisEndTime(Date diagnosisEndTime) {
        this.diagnosisEndTime = diagnosisEndTime;
    }

    public Date getDiagnosisStartTime() {
        return diagnosisStartTime;
    }

    public void setDiagnosisStartTime(Date diagnosisStartTime) {
        this.diagnosisStartTime = diagnosisStartTime;
    }

    public String getDiagnosisResourceType() {
        return diagnosisResourceType;
    }

    public void setDiagnosisResourceType(String diagnosisResourceType) {
        this.diagnosisResourceType = diagnosisResourceType;
    }

    public Integer getDiagnosisFrom() {
        return diagnosisFrom;
    }

    public void setDiagnosisFrom(Integer diagnosisFrom) {
        this.diagnosisFrom = diagnosisFrom;
    }

    public Integer getDiagnosisParallel() {
        return diagnosisParallel;
    }

    public void setDiagnosisParallel(Integer diagnosisParallel) {
        this.diagnosisParallel = diagnosisParallel;
    }

    public Integer getDiagnosisJmMemSize() {
        return diagnosisJmMemSize;
    }

    public void setDiagnosisJmMemSize(Integer diagnosisJmMemSize) {
        this.diagnosisJmMemSize = diagnosisJmMemSize;
    }

    public Integer getDiagnosisTmMemSize() {
        return diagnosisTmMemSize;
    }

    public void setDiagnosisTmMemSize(Integer diagnosisTmMemSize) {
        this.diagnosisTmMemSize = diagnosisTmMemSize;
    }

    public Integer getDiagnosisTmSlotNum() {
        return diagnosisTmSlotNum;
    }

    public void setDiagnosisTmSlotNum(Integer diagnosisTmSlotNum) {
        this.diagnosisTmSlotNum = diagnosisTmSlotNum;
    }

    public Integer getDiagnosisTmCoreNum() {
        return diagnosisTmCoreNum;
    }

    public void setDiagnosisTmCoreNum(Integer diagnosisTmCoreNum) {
        this.diagnosisTmCoreNum = diagnosisTmCoreNum;
    }

    public Integer getDiagnosisTmNum() {
        return diagnosisTmNum;
    }

    public void setDiagnosisTmNum(Integer diagnosisTmNum) {
        this.diagnosisTmNum = diagnosisTmNum;
    }

    public String getDiagnosisTypes() {
        return diagnosisTypes;
    }

    public void setDiagnosisTypes(String diagnosisTypes) {
        this.diagnosisTypes = diagnosisTypes;
    }

    public String getProcessState() {
        return processState;
    }

    public void setProcessState(String processState) {
        this.processState = processState;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", flinkTaskAppId=").append(flinkTaskAppId);
        sb.append(", username=").append(username);
        sb.append(", userId=").append(userId);
        sb.append(", projectName=").append(projectName);
        sb.append(", projectId=").append(projectId);
        sb.append(", flowName=").append(flowName);
        sb.append(", flowId=").append(flowId);
        sb.append(", taskName=").append(taskName);
        sb.append(", taskId=").append(taskId);
        sb.append(", applicationId=").append(applicationId);
        sb.append(", flinkTrackUrl=").append(flinkTrackUrl);
        sb.append(", allocatedMb=").append(allocatedMb);
        sb.append(", allocatedVcores=").append(allocatedVcores);
        sb.append(", runningContainers=").append(runningContainers);
        sb.append(", engineType=").append(engineType);
        sb.append(", executionTime=").append(executionTime);
        sb.append(", duration=").append(duration);
        sb.append(", startTime=").append(startTime);
        sb.append(", endTime=").append(endTime);
        sb.append(", vcoreSeconds=").append(vcoreSeconds);
        sb.append(", memorySeconds=").append(memorySeconds);
        sb.append(", queue=").append(queue);
        sb.append(", clusterName=").append(clusterName);
        sb.append(", retryTimes=").append(retryTimes);
        sb.append(", executeUser=").append(executeUser);
        sb.append(", diagnosis=").append(diagnosis);
        sb.append(", parallel=").append(parallel);
        sb.append(", tmSlot=").append(tmSlot);
        sb.append(", tmCore=").append(tmCore);
        sb.append(", tmMem=").append(tmMem);
        sb.append(", jmMem=").append(jmMem);
        sb.append(", tmNum=").append(tmNum);
        sb.append(", jobName=").append(jobName);
        sb.append(", diagnosisEndTime=").append(diagnosisEndTime);
        sb.append(", diagnosisStartTime=").append(diagnosisStartTime);
        sb.append(", diagnosisResourceType=").append(diagnosisResourceType);
        sb.append(", diagnosisFrom=").append(diagnosisFrom);
        sb.append(", diagnosisParallel=").append(diagnosisParallel);
        sb.append(", diagnosisJmMemSize=").append(diagnosisJmMemSize);
        sb.append(", diagnosisTmMemSize=").append(diagnosisTmMemSize);
        sb.append(", diagnosisTmSlotNum=").append(diagnosisTmSlotNum);
        sb.append(", diagnosisTmCoreNum=").append(diagnosisTmCoreNum);
        sb.append(", diagnosisTmNum=").append(diagnosisTmNum);
        sb.append(", diagnosisTypes=").append(diagnosisTypes);
        sb.append(", processState=").append(processState);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}