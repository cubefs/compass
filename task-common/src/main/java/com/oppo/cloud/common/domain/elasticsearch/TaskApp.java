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

package com.oppo.cloud.common.domain.elasticsearch;

import com.oppo.cloud.common.constant.AppCategoryEnum;
import com.oppo.cloud.common.constant.ApplicationType;
import com.oppo.cloud.common.constant.Constant;
import com.oppo.cloud.common.domain.cluster.spark.SparkApp;
import com.oppo.cloud.common.domain.cluster.yarn.YarnApp;
import com.oppo.cloud.common.domain.mr.MRJobHistoryLogPath;
import com.oppo.cloud.common.service.RedisService;
import com.oppo.cloud.common.util.DateUtil;
import com.oppo.cloud.common.util.LogPathUtil;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

@Data
public class TaskApp extends EsInfo {

    @ApiModelProperty(value = "applicationId名称")
    private String applicationId;

    @ApiModelProperty(value = "applicationType类型")
    private String applicationType;

    @ApiModelProperty(value = "执行用户")
    private String executeUser;

    @ApiModelProperty(value = "执行队列")
    private String queue;

    @ApiModelProperty(value = "集群名称")
    private String clusterName;

    @ApiModelProperty(value = "用户列表")
    private List<SimpleUser> users;

    @ApiModelProperty(value = "项目名称")
    private String projectName;

    @ApiModelProperty(value = "项目ID")
    private Integer projectId;

    @ApiModelProperty(value = "工作流名称")
    private String flowName;

    @ApiModelProperty(value = "工作流Id")
    private Integer flowId;

    @ApiModelProperty(value = "任务名称")
    private String taskName;

    @ApiModelProperty(value = "任务Id")
    private Integer taskId;

    @ApiModelProperty(value = "执行周期")
    private Date executionDate;

    @ApiModelProperty(value = "任务开始执行时间")
    private Date startTime;

    @ApiModelProperty(value = "任务结束时间")
    private Date finishTime;

    @ApiModelProperty(value = "任务执行耗时")
    private Double elapsedTime;

    @ApiModelProperty(value = "任务执行状态")
    private String taskAppState;

    @ApiModelProperty(value = "执行消耗memory·seconds[mb·s]")
    private Double memorySeconds;

    @ApiModelProperty(value = "执行消耗vcore·seconds")
    private Double vcoreSeconds;

    @ApiModelProperty(value = "am异常信息")
    private String diagnostics;

    @ApiModelProperty(value = "第几次重试的下的app")
    private Integer retryTimes;

    @ApiModelProperty(value = "异常类型")
    private List<String> categories;

    @ApiModelProperty(value = "任务处理状态")
    private Integer taskStatus = 0;

    @ApiModelProperty(value = "诊断结果")
    private String diagnoseResult;

    @ApiModelProperty(value = "跳转SparkUI")
    private String sparkUI;

    @ApiModelProperty(value = "spark event log path")
    private String eventLogPath;

    @ApiModelProperty(value = "yarn container log path")
    private String yarnLogPath;

    @ApiModelProperty(value = "mr job history done path")
    private String jobHistoryDoneLogPath;

    @ApiModelProperty(value = "mr job history intermediate done path")
    private String jobHistoryIntermediateDoneLogPath;

    @ApiModelProperty(value = "am 主机名")
    private String amHost;

    @ApiModelProperty(value = "是否删除")
    private Integer deleted = 0;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "修改时间")
    private Date updateTime;

    public Map<String, Object> genDoc() throws Exception {
        Map<String, Object> res = new HashMap<>();
        Field[] fileds = this.getClass().getDeclaredFields();
        for (Field field : fileds) {
            String key = field.getName();
            String method = key.substring(0, 1).toUpperCase() + key.substring(1);
            Method getMethod = this.getClass().getMethod("get" + method);
            switch (field.getName()) {
                case "docId":
                    break;
                case "executionDate":
                case "startTime":
                case "finishTime":
                case "updateTime":
                case "createTime":
                    Date value = (Date) getMethod.invoke(this);
                    if (value != null) {
                        res.put(key, DateUtil.timestampToUTCDate(value.getTime()));
                    }
                    break;
                default:
                    res.put(key, getMethod.invoke(this));
            }
        }
        return res;
    }

    public String genIndex(String baseIndex) {
        return StringUtils.isNotBlank(this.getIndex()) ? this.getIndex()
                : baseIndex + "-" + DateUtil.format(this.getExecutionDate(), "yyyy-MM-dd");
    }

    public String genDocId() {
        return StringUtils.isNotBlank(this.getDocId()) ? this.getDocId() : UUID.randomUUID().toString();
    }

    public void updateTaskApp(YarnApp yarnApp, SparkApp sparkApp, RedisService redisService) throws Exception {
        this.applicationId = yarnApp.getId();
        this.applicationType = yarnApp.getApplicationType();
        this.executeUser = yarnApp.getUser();
        this.queue = yarnApp.getQueue();
        this.clusterName = yarnApp.getClusterName();
        this.startTime = new Date(yarnApp.getStartedTime());
        this.finishTime = new Date(yarnApp.getFinishedTime());
        this.elapsedTime = (double) yarnApp.getElapsedTime();
        this.diagnostics = yarnApp.getDiagnostics();
        this.diagnoseResult = StringUtils.isNotBlank(yarnApp.getDiagnostics()) ? "abnormal" : "";
        this.categories = StringUtils.isNotBlank(yarnApp.getDiagnostics())
                ? Collections.singletonList(AppCategoryEnum.OTHER_EXCEPTION.getCategory())
                : new ArrayList<>();
        this.executeUser = yarnApp.getUser();
        this.vcoreSeconds = (double) yarnApp.getVcoreSeconds();
        this.taskAppState = yarnApp.getFinalStatus();
        this.setMemorySeconds((double) Math.round(yarnApp.getMemorySeconds()));
        String[] amHost = yarnApp.getAmHostHttpAddress().split(":");
        if (amHost.length == 0) {
            throw new Exception(String.format("parse amHost error, amHost:%s", yarnApp.getAmHostHttpAddress()));
        }
        this.amHost = amHost[0];
        if (sparkApp != null) {
            String attemptId = StringUtils.isNotEmpty(sparkApp.getAttemptId()) ? sparkApp.getAttemptId() : "1";
            this.eventLogPath = sparkApp.getEventLogDirectory() + "/" + this.applicationId + "_" + attemptId;
        }
        if (ApplicationType.MAPREDUCE.getValue().equals(yarnApp.getApplicationType())) {
            MRJobHistoryLogPath mrJobHistoryLogPath = LogPathUtil.getMRJobHistoryDoneLogPath(yarnApp, redisService);
            this.jobHistoryDoneLogPath = mrJobHistoryLogPath.getDoneLogPath();
            this.jobHistoryIntermediateDoneLogPath = mrJobHistoryLogPath.getIntermediateDoneLogPath();
        }

        String yarnLogPath = LogPathUtil.getYarnLogPath(Constant.JHS_HDFS_PATH, yarnApp.getIp(), redisService);
        if ("".equals(yarnLogPath)) {
            throw new Exception(String.format("can not find yarn log path: rm ip : %s", yarnApp.getIp()));
        }
        // todo adapt to different hadoop versions
        this.yarnLogPath = yarnLogPath + "/" + yarnApp.getUser() + "/logs/" + this.applicationId;

    }


}
