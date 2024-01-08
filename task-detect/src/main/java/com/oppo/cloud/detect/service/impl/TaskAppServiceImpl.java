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

package com.oppo.cloud.detect.service.impl;

import com.oppo.cloud.common.constant.ApplicationType;
import com.oppo.cloud.common.domain.cluster.spark.SparkApp;
import com.oppo.cloud.common.domain.cluster.yarn.YarnApp;
import com.oppo.cloud.common.domain.opensearch.JobAnalysis;
import com.oppo.cloud.common.domain.opensearch.TaskApp;
import com.oppo.cloud.common.service.RedisService;
import com.oppo.cloud.common.util.DateUtil;
import com.oppo.cloud.common.util.ui.TryNumberUtil;
import com.oppo.cloud.detect.domain.AbnormalTaskAppInfo;
import com.oppo.cloud.detect.service.OpenSearchService;
import com.oppo.cloud.detect.service.TaskAppService;
import com.oppo.cloud.mapper.TaskApplicationMapper;
import com.oppo.cloud.model.TaskApplication;
import com.oppo.cloud.model.TaskApplicationExample;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Exception task App interface class.
 */
@Service
@Slf4j
public class TaskAppServiceImpl implements TaskAppService {

    @Autowired
    private TaskApplicationMapper taskApplicationMapper;

    @Autowired
    private OpenSearchService openSearchService;

    @Autowired
    private RedisService redisService;

    @Value("${custom.opensearch.app-index}")
    private String appIndex;

    @Value("${custom.schedulerType}")
    private String schedulerType;

    @Value(value = "${spark.io.compression.codec}")
    private String sparkCompressionCodec;

    /**
     * Get the list information of exception task Apps.
     */
    @Override
    public AbnormalTaskAppInfo getAbnormalTaskAppsInfo(JobAnalysis jobAnalysis, String handledApps) {
        AbnormalTaskAppInfo abnormalTaskAppInfo = new AbnormalTaskAppInfo();
        List<TaskApp> taskAppList = new ArrayList<>();
        // Collect exception information for each appId.
        StringBuilder exceptionInfo = new StringBuilder();
        // Newly processed taskApps in this round.
        StringBuilder handledAppsNew = new StringBuilder();
        // Determine whether the appId of each retry of the task has been found.
        Map<Integer, Boolean> needed = new HashMap<>();
        for (int i = 0; i <= jobAnalysis.getRetryTimes(); i++) {
            needed.put(i, false);
        }

        List<TaskApplication> taskApplicationList = getTaskApplications(jobAnalysis.getProjectName(),
                jobAnalysis.getFlowName(), jobAnalysis.getTaskName(), jobAnalysis.getExecutionDate());

        for (TaskApplication taskApplication : taskApplicationList) {
            taskApplication.setRetryTimes(TryNumberUtil.updateTryNumber(taskApplication.getRetryTimes(), schedulerType));
            try {
                if (handledApps != null && handledApps.contains(taskApplication.getApplicationId())) {
                    // This appId has already been processed.
                    needed.put(taskApplication.getRetryTimes(), true);
                    continue;
                }
                if (needed.containsKey(taskApplication.getRetryTimes())) {
                    needed.put(taskApplication.getRetryTimes(), true);
                } else {
                    // Compatible with manually executed tasks, all retries are treated as the first retry of a different cycle.
                    taskApplication.setRetryTimes(0);
                    needed.put(0, true);
                }
                // Construct a TaskApp (including related log paths) based on the appId.
                TaskApp taskApp = this.buildAbnormalTaskApp(taskApplication);
                // Update the metadata information to the taskApp.
                taskApp.setTaskId(jobAnalysis.getTaskId());
                taskApp.setFlowId(jobAnalysis.getFlowId());
                taskApp.setProjectId(jobAnalysis.getProjectId());
                taskApp.setUsers(jobAnalysis.getUsers());
                taskAppList.add(taskApp);
                handledAppsNew.append(taskApp.getApplicationId()).append(";");
            } catch (Exception e) {
                exceptionInfo.append(e.getMessage()).append(";");
            }
        }
        List<String> notFound = new ArrayList<>();
        // Mark the number of retries for the appId that has been queried.
        for (Integer taskTryNum : needed.keySet()) {
            if (!needed.get(taskTryNum)) {
                notFound.add(String.valueOf(taskTryNum));
            }
        }
        // Determine whether all appIds under all retry counts have been found.
        if (notFound.size() > 0) {
            exceptionInfo.append(String.format("can not find appId by tryNum: %s", String.join(",", notFound)));
        }
        abnormalTaskAppInfo.setTaskAppList(taskAppList);
        abnormalTaskAppInfo.setExceptionInfo(exceptionInfo.toString());
        abnormalTaskAppInfo.setHandleApps(handledAppsNew.toString());
        return abnormalTaskAppInfo;
    }

    /**
     * Get the appId under the exception task, including those with or without.
     */

    @Override
    public Map<Integer, List<TaskApp>> getAbnormalTaskApps(JobAnalysis jobAnalysis) {
        Map<Integer, List<TaskApp>> res = new HashMap<>();
        List<TaskApplication> taskApplicationList = getTaskApplications(jobAnalysis.getProjectName(),
                jobAnalysis.getFlowName(), jobAnalysis.getTaskName(), jobAnalysis.getExecutionDate());
        // Construct all retry records based on the number of retries.
        for (int i = 0; i <= jobAnalysis.getRetryTimes(); i++) {
            List<TaskApp> temp = new ArrayList<>();
            res.put(i, temp);
        }
        // Query all appIds.
        for (TaskApplication taskApplication : taskApplicationList) {
            TaskApp taskApp = this.tryBuildAbnormalTaskApp(taskApplication);
            List<TaskApp> temp;
            if (res.containsKey(taskApplication.getRetryTimes())) {
                temp = res.get(taskApplication.getRetryTimes());
                temp.add(taskApp);
                res.put(taskApplication.getRetryTimes(), temp);
            } else {
                // If not included, place this appId in the first retry.
                temp = new ArrayList<>();
                taskApp.setRetryTimes(0);
                temp.add(taskApp);
                res.put(0, temp);
            }
        }
        // Construct an empty one if there is no appId.
        for (Integer tryTime : res.keySet()) {
            List<TaskApp> temp = res.get(tryTime);
            if (temp.size() == 0) {
                TaskApp taskApp = new TaskApp();
                taskApp.setRetryTimes(tryTime);
                taskApp.setTaskName(jobAnalysis.getTaskName());
                taskApp.setFlowName(jobAnalysis.getFlowName());
                taskApp.setProjectName(jobAnalysis.getProjectName());
                taskApp.setExecutionDate(jobAnalysis.getExecutionDate());
                temp.add(taskApp);
                res.put(tryTime, temp);
            }
        }
        return res;
    }

    @Override
    public void insertTaskApps(List<TaskApp> taskAppList) throws Exception {
        for (TaskApp taskApp : taskAppList) {
            String index = taskApp.genIndex(appIndex);
            log.info("insertTaskApp {},{},{}", index, taskApp.getApplicationId(), taskApp.genDoc());
            openSearchService.insertOrUpDate(index, taskApp.genDocId(), taskApp.genDoc());
        }
    }

    @Override
    public List<TaskApp> searchTaskApps(JobAnalysis jobAnalysis) throws Exception {
        HashMap<String, Object> termCondition = new HashMap<>();
        termCondition.put("projectName.keyword", jobAnalysis.getProjectName());
        termCondition.put("flowName.keyword", jobAnalysis.getFlowName());
        termCondition.put("taskName.keyword", jobAnalysis.getTaskName());
        termCondition.put("executionDate", DateUtil.timestampToUTCDate(jobAnalysis.getExecutionDate().getTime()));
        SearchSourceBuilder searchSourceBuilder =
                openSearchService.genSearchBuilder(termCondition, null, null, null);
        return openSearchService.find(TaskApp.class, searchSourceBuilder, appIndex + "-*");
    }

    /**
     * Construct an AbnormalTaskApp based on the basic appId information. If there is an exception, exit directly and throw an exception.
     */
    public TaskApp buildAbnormalTaskApp(TaskApplication taskApplication) throws Exception {
        TaskApp taskApp = new TaskApp();
        BeanUtils.copyProperties(taskApplication, taskApp);
        taskApp.setExecutionDate(taskApplication.getExecuteTime());

        YarnApp yarnApp = openSearchService.searchYarnApp(taskApplication.getApplicationId());

        SparkApp sparkApp = null;
        if (ApplicationType.SPARK.getValue().equals(yarnApp.getApplicationType())) {
            sparkApp = openSearchService.searchSparkApp(taskApplication.getApplicationId());
        }

        taskApp.updateTaskApp(yarnApp, sparkApp, redisService, sparkCompressionCodec);

        return taskApp;
    }


    public TaskApp tryBuildAbnormalTaskApp(TaskApplication taskApplication) {
        TaskApp taskApp = new TaskApp();
        BeanUtils.copyProperties(taskApplication, taskApp);
        taskApp.setExecutionDate(taskApplication.getExecuteTime());
        taskApp.setRetryTimes(taskApplication.getRetryTimes());
        try {
            YarnApp yarnApp = openSearchService.searchYarnApp(taskApplication.getApplicationId());
            taskApp.setStartTime(new Date(yarnApp.getStartedTime()));
            taskApp.setFinishTime(new Date(yarnApp.getFinishedTime()));
            taskApp.setElapsedTime((double) yarnApp.getElapsedTime());
            taskApp.setClusterName(yarnApp.getClusterName());
            taskApp.setApplicationType(yarnApp.getApplicationType());
            taskApp.setQueue(yarnApp.getQueue());
            taskApp.setDiagnoseResult(yarnApp.getDiagnostics());
            taskApp.setExecuteUser(yarnApp.getUser());
            taskApp.setVcoreSeconds((double) yarnApp.getVcoreSeconds());
            taskApp.setMemorySeconds((double) Math.round(yarnApp.getMemorySeconds()));
            taskApp.setTaskAppState(yarnApp.getState());
            String[] amHost = yarnApp.getAmHostHttpAddress().split(":");
            if (amHost.length != 0) {
                taskApp.setAmHost(amHost[0]);
            }
        } catch (Exception e) {
            log.error("try complete yarn info failed, msg:", e);
        }
        try {
            SparkApp sparkApp = openSearchService.searchSparkApp(taskApplication.getApplicationId());
            taskApp.setEventLogPath(sparkApp.getEventLogDirectory() + "/" + taskApplication.getApplicationId());
        } catch (Exception e) {
            log.error("try complete spark info failed, msg:", e);
        }
        return taskApp;
    }


    public List<TaskApplication> getTaskApplications(String projectName, String flowName, String taskName,
                                                     Date executionTime) {
        TaskApplicationExample taskApplicationExample = new TaskApplicationExample();
        taskApplicationExample.createCriteria()
                .andProjectNameEqualTo(projectName)
                .andFlowNameEqualTo(flowName)
                .andTaskNameEqualTo(taskName)
                .andExecuteTimeEqualTo(executionTime)
                .andApplicationIdIsNotNull();
        return taskApplicationMapper.selectByExample(taskApplicationExample);
    }
}
