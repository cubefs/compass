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

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.oppo.cloud.common.constant.AppCategoryEnum;
import com.oppo.cloud.common.constant.ApplicationType;
import com.oppo.cloud.common.constant.Constant;
import com.oppo.cloud.common.domain.cluster.spark.SparkApp;
import com.oppo.cloud.common.domain.cluster.yarn.YarnApp;
import com.oppo.cloud.common.domain.elasticsearch.JobAnalysis;
import com.oppo.cloud.common.domain.elasticsearch.TaskApp;
import com.oppo.cloud.common.service.RedisService;
import com.oppo.cloud.common.util.DateUtil;
import com.oppo.cloud.common.util.MRUtil;
import com.oppo.cloud.common.util.ui.TryNumberUtil;
import com.oppo.cloud.detect.domain.AbnormalTaskAppInfo;
import com.oppo.cloud.detect.service.*;
import com.oppo.cloud.mapper.TaskApplicationMapper;
import com.oppo.cloud.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 异常任务App接口类
 */
@Service
@Slf4j
public class TaskAppServiceImpl implements TaskAppService {

    @Autowired
    private TaskApplicationMapper taskApplicationMapper;

    @Autowired
    private ElasticSearchService elasticSearchService;

    @Autowired
    private RedisService redisService;

    /**
     * sparkUi代理前缀
     */
    @Value("${custom.sparkUiProxy.url}")
    private String sparkUiProxy;

    @Value("${custom.elasticsearch.app-index}")
    private String appIndex;

    @Value("${custom.schedulerType}")
    private String schedulerType;

    /**
     * 获取异常任务的App列表信息
     */
    @Override
    public AbnormalTaskAppInfo getAbnormalTaskAppsInfo(JobAnalysis jobAnalysis, String handledApps) {
        AbnormalTaskAppInfo abnormalTaskAppInfo = new AbnormalTaskAppInfo();
        List<TaskApp> taskAppList = new ArrayList<>();
        // 收集每个appId的异常信息
        StringBuilder exceptionInfo = new StringBuilder();
        // 本次新处理的taskApps
        StringBuilder handledAppsNew = new StringBuilder();
        // 判断任务每次重试的appId是否已经找到
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
                    // 该appId已经被处理
                    needed.put(taskApplication.getRetryTimes(), true);
                    continue;
                }
                if (needed.containsKey(taskApplication.getRetryTimes())) {
                    needed.put(taskApplication.getRetryTimes(), true);
                } else {
                    // 兼容手动执行的任务，所有的重试当成不同周期的第一次重试
                    taskApplication.setRetryTimes(0);
                    needed.put(0, true);
                }
                // 根据appId构造TaskApp(包括相关的日志路径)
                TaskApp taskApp = this.buildAbnormalTaskApp(taskApplication);
                // 将元数据信息更新到taskApp中
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
        // 标志已经查询到的appId的重试次数
        for (Integer taskTryNum : needed.keySet()) {
            if (!needed.get(taskTryNum)) {
                notFound.add(String.valueOf(taskTryNum));
            }
        }
        // 判断是否查到所有重试次数下的appId
        if (notFound.size() > 0) {
            exceptionInfo.append(String.format("can not find appId by tryNum: %s", String.join(",", notFound)));
        }
        abnormalTaskAppInfo.setTaskAppList(taskAppList);
        abnormalTaskAppInfo.setExceptionInfo(exceptionInfo.toString());
        abnormalTaskAppInfo.setHandleApps(handledAppsNew.toString());
        return abnormalTaskAppInfo;
    }

    /**
     * 获取异常任务下的appId,包括有或没有的
     */

    @Override
    public Map<Integer, List<TaskApp>> getAbnormalTaskApps(JobAnalysis jobAnalysis) {
        Map<Integer, List<TaskApp>> res = new HashMap<>();
        List<TaskApplication> taskApplicationList = getTaskApplications(jobAnalysis.getProjectName(),
                jobAnalysis.getFlowName(), jobAnalysis.getTaskName(), jobAnalysis.getExecutionDate());
        // 根据重试次数构建出所有的重试记录
        for (int i = 0; i <= jobAnalysis.getRetryTimes(); i++) {
            List<TaskApp> temp = new ArrayList<>();
            res.put(i, temp);
        }
        // 查询出来所有的appIds
        for (TaskApplication taskApplication : taskApplicationList) {
            TaskApp taskApp = this.tryBuildAbnormalTaskApp(taskApplication);
            List<TaskApp> temp;
            if (res.containsKey(taskApplication.getRetryTimes())) {
                temp = res.get(taskApplication.getRetryTimes());
                temp.add(taskApp);
                res.put(taskApplication.getRetryTimes(), temp);
            } else {
                // 不包含则将这个appId放在第一次重试中
                temp = new ArrayList<>();
                taskApp.setRetryTimes(0);
                temp.add(taskApp);
                res.put(0, temp);
            }
        }
        // 没有appId的构造为空的
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
            elasticSearchService.insertOrUpDateEs(index, taskApp.genDocId(), taskApp.genDoc());
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
                elasticSearchService.genSearchBuilder(termCondition, null, null, null);
        return elasticSearchService.find(TaskApp.class, searchSourceBuilder, appIndex + "-*");
    }

    /**
     * 根据基础的appId信息构建出AbnormalTaskApp,有异常则直接退出抛出异常
     */
    public TaskApp buildAbnormalTaskApp(TaskApplication taskApplication) throws Exception {
        TaskApp taskApp = new TaskApp();
        BeanUtils.copyProperties(taskApplication, taskApp);
        YarnApp yarnApp = elasticSearchService.searchYarnApp(taskApplication.getApplicationId());
        setDefaultTaskApp(taskApp, taskApplication, yarnApp);

        if (ApplicationType.SPARK.getValue().equals(yarnApp.getApplicationType())) {
            SparkApp sparkApp = elasticSearchService.searchSparkApp(taskApplication.getApplicationId());
            String attemptId = StringUtils.isNotEmpty(sparkApp.getAttemptId()) ? sparkApp.getAttemptId() : "1";
            taskApp.setEventLogPath(
                    sparkApp.getEventLogDirectory() + "/" + taskApplication.getApplicationId() + "_" + attemptId);
            taskApp.setSparkUI(
                    String.format(sparkUiProxy, sparkApp.getSparkHistoryServer(), taskApplication.getApplicationId()));
            String yarnLogPath = getYarnLogPath(Constant.JHS_HDFS_PATH, yarnApp.getIp());
            if ("".equals(yarnLogPath)) {
                throw new Exception(String.format("can not find yarn log path: rm ip : %s", yarnApp.getIp()));
            }
            taskApp.setYarnLogPath(yarnLogPath + "/" + yarnApp.getUser() + "/logs/" + taskApplication.getApplicationId());
        }

        if (ApplicationType.MAPREDUCE.getValue().equals(yarnApp.getApplicationType())) {
            String jobHistoryDoneLogPathPrefix = getYarnLogPath(Constant.JHS_MAPREDUCE_DONE_PATH, yarnApp.getIp());
            if ("".equals(jobHistoryDoneLogPathPrefix)) {
                throw new Exception(String.format("can not find mr job history log path: rm ip : %s", yarnApp.getIp()));
            }
            String logSubdirectory = MRUtil.getHistoryLogSubdirectory(yarnApp.getId(), yarnApp.getFinishedTime());
            String jobId = MRUtil.appIdToJobId(yarnApp.getId());
            String jobHistoryDoneLogPath = String.format("%s/%s%s*", jobHistoryDoneLogPathPrefix, logSubdirectory, jobId);
            taskApp.setJobHistoryDoneLogPath(jobHistoryDoneLogPath);
            String jobHistoryIntermediateDoneLogPathPrefix = getYarnLogPath(Constant.JHS_MAPREDUCE_INTERMEDIATE_DONE_PATH, yarnApp.getIp());
            String jobHistoryIntermediateDoneLogPath = String.format("%s/%s/%s*", jobHistoryIntermediateDoneLogPathPrefix, yarnApp.getUser(), jobId);
            taskApp.setJobHistoryIntermediateDoneLogPath(jobHistoryIntermediateDoneLogPath);
        }

        return taskApp;
    }

    public void setDefaultTaskApp(TaskApp taskApp, TaskApplication taskApplication, YarnApp yarnApp) throws Exception {
        taskApp.setExecutionDate(taskApplication.getExecuteTime());
        taskApp.setStartTime(new Date(yarnApp.getStartedTime()));
        taskApp.setFinishTime(new Date(yarnApp.getFinishedTime()));
        taskApp.setElapsedTime((double) yarnApp.getElapsedTime());
        taskApp.setClusterName(yarnApp.getClusterName());
        taskApp.setApplicationType(yarnApp.getApplicationType());
        taskApp.setQueue(yarnApp.getQueue());
        taskApp.setDiagnostics(yarnApp.getDiagnostics());
        taskApp.setDiagnoseResult(StringUtils.isNotBlank(yarnApp.getDiagnostics()) ? "abnormal" : "");
        taskApp.setCategories(StringUtils.isNotBlank(yarnApp.getDiagnostics())
                ? Collections.singletonList(AppCategoryEnum.OTHER_EXCEPTION.getCategory())
                : new ArrayList<>());
        taskApp.setExecuteUser(yarnApp.getUser());
        taskApp.setVcoreSeconds((double) yarnApp.getVcoreSeconds());
        taskApp.setTaskAppState(yarnApp.getFinalStatus());
        taskApp.setRetryTimes(taskApplication.getRetryTimes());
        taskApp.setMemorySeconds((double) Math.round(yarnApp.getMemorySeconds()));
        String[] amHost = yarnApp.getAmHostHttpAddress().split(":");
        if (amHost.length == 0) {
            throw new Exception(String.format("parse amHost error, amHost:%s", yarnApp.getAmHostHttpAddress()));
        }
        taskApp.setAmHost(amHost[0]);
    }

    public TaskApp tryBuildAbnormalTaskApp(TaskApplication taskApplication) {
        TaskApp taskApp = new TaskApp();
        BeanUtils.copyProperties(taskApplication, taskApp);
        taskApp.setExecutionDate(taskApplication.getExecuteTime());
        taskApp.setRetryTimes(taskApplication.getRetryTimes());
        try {
            YarnApp yarnApp = elasticSearchService.searchYarnApp(taskApplication.getApplicationId());
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
//            String yarnLogPath = getYarnLogPath(yarnApp.getIp());
//            if (!"".equals(yarnLogPath)) {
//                taskApp.setYarnLogPath(
//                        yarnLogPath + "/" + yarnApp.getUser() + "/logs/" + taskApplication.getApplicationId());
//            }
            String[] amHost = yarnApp.getAmHostHttpAddress().split(":");
            if (amHost.length != 0) {
                taskApp.setAmHost(amHost[0]);
            }
        } catch (Exception e) {
            log.error("try complete yarn info failed, msg:", e);
        }
        try {
            SparkApp sparkApp = elasticSearchService.searchSparkApp(taskApplication.getApplicationId());
            taskApp.setEventLogPath(sparkApp.getEventLogDirectory() + "/" + taskApplication.getApplicationId());
        } catch (Exception e) {
            log.error("try complete spark info failed, msg:", e);
        }
        return taskApp;
    }

    /**
     * 查询redis,获取yarn 日志路径
     */
    public String getYarnLogPath(String logType, String rmIp) throws Exception {
        if (redisService.hasKey(Constant.RM_JHS_MAP)) {
            Map<String, String> rmJhsMap = JSON.parseObject((String) redisService.get(Constant.RM_JHS_MAP),
                    new TypeReference<Map<String, String>>() {
                    });
            String jhsIp = rmJhsMap.get(rmIp);
            String key = logType + jhsIp;
            if (redisService.hasKey(key)) {
                return (String) redisService.get(key);
            } else {
                throw new Exception(String.format("search redis error,msg: can not find key %s, rmJhsMap:%s, rmIp:%s",
                        key, rmJhsMap, rmIp));
            }

        } else {
            throw new Exception(String.format("search redis error,msg: can not find key %s", Constant.RM_JHS_MAP));
        }
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
