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

package com.oppo.cloud.common.domain.opensearch;

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
public class TaskApp extends OpenSearchInfo {

    @ApiModelProperty(value = "applicationId")
    private String applicationId;

    @ApiModelProperty(value = "applicationType")
    private String applicationType;

    @ApiModelProperty(value = "Execute user")
    private String executeUser;

    @ApiModelProperty(value = "queue")
    private String queue;

    @ApiModelProperty(value = "cluster name")
    private String clusterName;

    @ApiModelProperty(value = "Users")
    private List<SimpleUser> users;

    @ApiModelProperty(value = "Project name")
    private String projectName;

    @ApiModelProperty(value = "Project ID")
    private Integer projectId;

    @ApiModelProperty(value = "Flow name")
    private String flowName;

    @ApiModelProperty(value = "Flow Id")
    private Integer flowId;

    @ApiModelProperty(value = "Task name")
    private String taskName;

    @ApiModelProperty(value = "Task Id")
    private Integer taskId;

    @ApiModelProperty(value = "Execution date")
    private Date executionDate;

    @ApiModelProperty(value = "Start time of the task")
    private Date startTime;

    @ApiModelProperty(value = "End time of the task")
    private Date finishTime;

    @ApiModelProperty(value = "Running time")
    private Double elapsedTime;

    @ApiModelProperty(value = "Task execution status")
    private String taskAppState;

    @ApiModelProperty(value = "Memory consumption(Unit: memory·seconds[mb·s])")
    private Double memorySeconds;

    @ApiModelProperty(value = "CPU consumption (Unit: vcore·seconds)")
    private Double vcoreSeconds;

    @ApiModelProperty(value = "AM diagnosis information")
    private String diagnostics;

    @ApiModelProperty(value = "The app retried for the nth time")
    private Integer retryTimes;

    @ApiModelProperty(value = "Categories of exception")
    private List<String> categories;

    @ApiModelProperty(value = "Task processing status")
    private Integer taskStatus = 0;

    @ApiModelProperty(value = "Result of diagnosis")
    private String diagnoseResult;

    @ApiModelProperty(value = "SparkUI link")
    private String sparkUI;

    @ApiModelProperty(value = "Spark event log path")
    private String eventLogPath;

    @ApiModelProperty(value = "Yarn container log path")
    private String yarnLogPath;

    @ApiModelProperty(value = "MR job history done path")
    private String jobHistoryDoneLogPath;

    @ApiModelProperty(value = "MR job history intermediate done path")
    private String jobHistoryIntermediateDoneLogPath;

    @ApiModelProperty(value = "MR job history staging path")
    private String jobHistoryStagingLogPath;

    @ApiModelProperty(value = "AM host")
    private String amHost;

    @ApiModelProperty(value = "Delete or not")
    private Integer deleted = 0;

    @ApiModelProperty(value = "Create time")
    private Date createTime;

    @ApiModelProperty(value = "Update time")
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
        this.finishTime = new Date(yarnApp.getFinishedTime() == 0 ? System.currentTimeMillis() : yarnApp.getFinishedTime());
        this.elapsedTime = (double) yarnApp.getElapsedTime();
        this.diagnostics = yarnApp.getDiagnostics();
        this.diagnoseResult = StringUtils.isNotBlank(yarnApp.getDiagnostics()) ? "abnormal" : "";
        this.categories = StringUtils.isNotBlank(yarnApp.getDiagnostics())
                ? Collections.singletonList(AppCategoryEnum.OTHER_EXCEPTION.getCategory())
                : new ArrayList<>();
        this.executeUser = yarnApp.getUser();
        this.vcoreSeconds = (double) yarnApp.getVcoreSeconds();
        this.taskAppState = yarnApp.getState();
        this.setMemorySeconds((double) Math.round(yarnApp.getMemorySeconds()));
        String[] amHost = yarnApp.getAmHostHttpAddress().split(":");
        if (amHost.length == 0) {
            throw new Exception(String.format("parse amHost error, amHost:%s", yarnApp.getAmHostHttpAddress()));
        }
        this.amHost = amHost[0];
        if (sparkApp != null) {
            this.eventLogPath = LogPathUtil.getSparkEventLogPath(sparkApp.getEventLogDirectory(), this.applicationId,
                    sparkApp.getAttemptId(), yarnApp.getState());
        }
        if (ApplicationType.MAPREDUCE.getValue().equals(yarnApp.getApplicationType())) {
            MRJobHistoryLogPath mrJobHistoryLogPath = LogPathUtil.getMRJobHistoryDoneLogPath(yarnApp, redisService);
            this.jobHistoryDoneLogPath = mrJobHistoryLogPath.getDoneLogPath();
            this.jobHistoryIntermediateDoneLogPath = mrJobHistoryLogPath.getIntermediateDoneLogPath();
            this.jobHistoryStagingLogPath = mrJobHistoryLogPath.getStagingLogPath();
        }

        String yarnLogPath = LogPathUtil.getYarnLogPath(Constant.JHS_HDFS_PATH, yarnApp.getIp(), redisService);
        if ("".equals(yarnLogPath)) {
            throw new Exception(String.format("can not find yarn log path: rm ip : %s", yarnApp.getIp()));
        }
        // Todo: adapt to different hadoop versions
        this.yarnLogPath = yarnLogPath + "/" + yarnApp.getUser() + "/logs/" + this.applicationId;

    }
}
