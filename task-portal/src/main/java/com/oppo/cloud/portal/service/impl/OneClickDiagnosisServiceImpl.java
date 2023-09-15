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

package com.oppo.cloud.portal.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.oppo.cloud.common.constant.*;
import com.oppo.cloud.common.domain.cluster.spark.SparkApp;
import com.oppo.cloud.common.domain.cluster.yarn.YarnApp;
import com.oppo.cloud.common.domain.opensearch.JobAnalysis;
import com.oppo.cloud.common.domain.opensearch.JobInstance;
import com.oppo.cloud.common.domain.opensearch.SimpleUser;
import com.oppo.cloud.common.domain.opensearch.TaskApp;
import com.oppo.cloud.common.domain.job.App;
import com.oppo.cloud.common.domain.job.LogRecord;
import com.oppo.cloud.common.domain.oneclick.OneClickProgress;
import com.oppo.cloud.common.service.RedisService;
import com.oppo.cloud.mapper.TaskApplicationMapper;
import com.oppo.cloud.mapper.TaskMapper;
import com.oppo.cloud.mapper.UserMapper;
import com.oppo.cloud.model.*;
import com.oppo.cloud.portal.common.CommonCode;
import com.oppo.cloud.portal.domain.diagnose.oneclick.DiagnoseResult;
import com.oppo.cloud.portal.domain.task.TaskAppInfo;
import com.oppo.cloud.portal.service.OpenSearchService;
import com.oppo.cloud.portal.service.JobService;
import com.oppo.cloud.portal.service.OneClickDiagnosisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class OneClickDiagnosisServiceImpl implements OneClickDiagnosisService {

    @Value(value = "${custom.opensearch.appIndex.name}")
    private String taskAppsIndex;

    @Value("${custom.redis.logRecordKey}")
    private String logRecordKey;

    @Value(value = "${custom.opensearch.yarnIndex.name}")
    private String yarnAppIndex;

    @Value(value = "${custom.opensearch.sparkIndex.name}")
    private String sparkAppIndex;

    @Autowired
    private RedisService redisService;

    @Autowired
    private OpenSearchService openSearchService;

    @Autowired
    private JobService jobService;

    @Autowired
    private TaskApplicationMapper taskApplicationMapper;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * one click diagnosis
     */
    @Override
    public DiagnoseResult diagnose(String applicationId) throws Exception {

        TaskApp taskApp = this.buildTaskApp(applicationId);
        if (!taskApp.getApplicationType().equals(ApplicationType.SPARK.getValue()) &&
                !taskApp.getApplicationType().equals(ApplicationType.MAPREDUCE.getValue())) {
            throw new Exception(String.format("暂不支持%s类型的任务", taskApp.getApplicationType()));
        }

        // Check if the application is diagnosing
        DiagnoseResult diagnoseResult = checkDiagnoseProgress(taskApp);
        if (diagnoseResult != null) {
            // Check the running state
            if (YarnAppState.RUNNING.toString().equals(taskApp.getTaskAppState()) &&
                    !ProgressState.PROCESSING.toString().equals(diagnoseResult.getStatus())) {
                return checkRunningStateTask(diagnoseResult, taskApp);
            }
            return diagnoseResult;
        }

        diagnoseResult = checkDiagnoseResult(applicationId);
        if (diagnoseResult != null) {
            return diagnoseResult;
        }

        return submitTask(taskApp);

    }

    private DiagnoseResult checkDiagnoseResult(String applicationId) throws Exception {
        List<TaskApp> taskAppList = findTaskApp(applicationId);
        if (taskAppList.size() != 0) {
            TaskApp taskApp = taskAppList.get(0);
            if (taskApp.getCategories() == null || taskApp.getCategories().size() == 0) {
                return null;
            }
            if (taskApp.getCategories().size() == 1 &&
                    taskApp.getCategories().contains(AppCategoryEnum.OTHER_EXCEPTION.getCategory()) &&
                    taskApp.getDiagnostics() != null) {
                return null;
            }
            TaskAppInfo taskAppInfo = TaskAppInfo.from(taskApp);
            DiagnoseResult diagnoseResult = new DiagnoseResult();
            diagnoseResult.setStatus(ProgressState.SUCCEED.toString());
            diagnoseResult.setTaskAppInfo(taskAppInfo);
            diagnoseResult.getProcessInfoList().add(new DiagnoseResult.ProcessInfo("该ApplicationId已经诊断完成", 100));
            return diagnoseResult;
        }
        return null;
    }

    private DiagnoseResult checkDiagnoseProgress(TaskApp taskApp) throws Exception {
        String taskAppTempKey = taskApp.getApplicationId() + CommonCode.TASK_APP_TEMP;
        if (!redisService.hasKey(taskAppTempKey)) {
            return null;
        }

        DiagnoseResult diagnoseResult = new DiagnoseResult();
        TaskAppInfo taskAppInfo = new TaskAppInfo();
        List<DiagnoseResult.ProcessInfo> processInfoList = new ArrayList<>();

        List<ProgressState> stateList = registerTaskProgressState(taskApp, processInfoList);

        if (checkFinalProgressState(stateList)) {
            if (checkFailedProgressState(stateList)) {
                // FAILED
                diagnoseResult.setStatus(ProgressState.FAILED.toString());
                diagnoseResult.setTaskAppInfo(null);
                diagnoseResult.setErrorMsg("");
            } else {
                // SUCCEED
                diagnoseResult.setStatus(ProgressState.SUCCEED.toString());
                List<TaskApp> taskAppList = findTaskApp(taskApp.getApplicationId());
                if (taskAppList.size() != 0) {
                    TaskApp result = taskAppList.get(0);
                    if (!taskApp.getTaskAppState().equals(result.getTaskAppState())) {
                        log.info("resubmitTask {},old state:{},new state:{}", taskApp.getApplicationId(),
                                result.getTaskAppState(), taskApp.getTaskAppState());
                        clearProgressStateCache(taskApp);
                        return submitTask(taskApp);
                    }
                    taskApp = result;
                }
                taskAppInfo = TaskAppInfo.from(taskApp);
            }
        } else {
            // PROCESSING
            diagnoseResult.setStatus(ProgressState.PROCESSING.toString());
            taskAppInfo = TaskAppInfo.from(taskApp);
            taskAppInfo.setCategories(new ArrayList<>());
        }

        diagnoseResult.setTaskAppInfo(taskAppInfo);
        diagnoseResult.setProcessInfoList(processInfoList);
        return diagnoseResult;
    }

    private List<ProgressState> registerTaskProgressState(TaskApp taskApp, List<DiagnoseResult.ProcessInfo> processInfoList) throws Exception {
        List<ProgressState> stateList = new ArrayList<>();
        if (ApplicationType.SPARK.getValue().equals(taskApp.getApplicationType())) {
            stateList.add(checkTaskParserProgress(LogType.SPARK_EVENT, taskApp.getApplicationId(), processInfoList));
            stateList.add(checkTaskParserProgress(LogType.SPARK_EXECUTOR, taskApp.getApplicationId(), processInfoList));
        }
        if (ApplicationType.MAPREDUCE.getValue().equals(taskApp.getApplicationType())) {
            stateList.add(checkTaskParserProgress(LogType.MAPREDUCE_JOB_HISTORY, taskApp.getApplicationId(), processInfoList));
            stateList.add(checkTaskParserProgress(LogType.MAPREDUCE_CONTAINER, taskApp.getApplicationId(), processInfoList));
        }
        return stateList;
    }


    private List<LogType> registerLogTypeList(TaskApp taskApp) {
        List<LogType> list = new ArrayList<>();
        if (ApplicationType.SPARK.getValue().equals(taskApp.getApplicationType())) {
            list.add(LogType.SPARK_EVENT);
            list.add(LogType.SPARK_EXECUTOR);
        }
        if (ApplicationType.MAPREDUCE.getValue().equals(taskApp.getApplicationType())) {
            list.add(LogType.MAPREDUCE_JOB_HISTORY);
            list.add(LogType.MAPREDUCE_CONTAINER);
        }
        return list;
    }


    private DiagnoseResult checkRunningStateTask(DiagnoseResult diagnoseResult, TaskApp taskApp) throws Exception {
        if (redisService.hasKey(taskApp.getApplicationId() + CommonCode.TASK_APP_RUNNING)) {
            return diagnoseResult;
        }
        clearProgressStateCache(taskApp);
        // diagnose again
        log.info("resubmitRunningTask {}", taskApp.getApplicationId());
        return submitTask(taskApp);
    }

    /**
     * check task-parser progress
     */
    private ProgressState checkTaskParserProgress(LogType logType, String applicationId, List<DiagnoseResult.ProcessInfo> processInfoList) throws Exception {
        String key = String.format("%s:%s", applicationId, logType.getName());
        OneClickProgress oneClickProgress;
        ProgressState state = ProgressState.PROCESSING;
        String msg;
        if (redisService.hasKey(key)) {
            oneClickProgress = JSON.parseObject((String) redisService.get(key), OneClickProgress.class);
            state = oneClickProgress.getProgressInfo().getState();
            Integer count = oneClickProgress.getProgressInfo().getCount();
            Integer process = oneClickProgress.getProgressInfo().getProgress();
            double speed = 0;
            switch (state) {
                case PROCESSING:
                    if (count != 0) {
                        speed = 100 * Double.parseDouble(String.format("%.2f", process / (double) count));
                    }
                    msg = String.format("%s 诊断中, 文件总数:%d, 已解析文件数:%d", logType.getDesc(), count, process);
                    processInfoList.add(new DiagnoseResult.ProcessInfo(msg, speed));
                    break;
                case SUCCEED:
                    msg = String.format("%s 诊断完成", logType.getDesc());
                    processInfoList.add(new DiagnoseResult.ProcessInfo(msg, 100));
                    break;
                case FAILED:
                    msg = String.format("%s 诊断失败, 请联系系统管理员", logType.getDesc());
                    processInfoList.add(new DiagnoseResult.ProcessInfo(msg, 100));
                    break;
                default:
                    break;
            }
        } else {
            processInfoList.add(new DiagnoseResult.ProcessInfo(applicationId + "发送诊断中, 请稍后", 0));
        }
        return state;
    }

    private DiagnoseResult submitTask(TaskApp taskApp) throws Exception {

        JobAnalysis jobAnalysis = new JobAnalysis();
        JobInstance jobInstance = jobService.getJobInstance(taskApp.getProjectName(), taskApp.getFlowName(), taskApp.getTaskName(), taskApp.getExecutionDate());
        if (jobInstance != null) {
            BeanUtils.copyProperties(jobInstance, jobAnalysis);
        } else {
            jobAnalysis.setExecutionDate(taskApp.getExecutionDate());
        }

        LogRecord logRecord = new LogRecord();
        logRecord.setJobAnalysis(jobAnalysis);
        App app = new App();
        app.formatAppLog(taskApp);
        logRecord.setApps(Collections.singletonList(app));
        logRecord.formatTaskAppList(Collections.singletonList(taskApp));

        List<TaskApp> taskAppEsList = findTaskApp(taskApp.getApplicationId());

        if (taskAppEsList.size() == 0) {
            List<String> categories = new ArrayList<>();
            if (StringUtils.isNotEmpty(taskApp.getDiagnostics())) {
                categories.add(AppCategoryEnum.OTHER_EXCEPTION.getCategory());
            }
            taskApp.setCategories(categories);
            openSearchService.insertOrUpDate(taskApp.genIndex(taskAppsIndex), taskApp.genDocId(), taskApp.genDoc());
        }

        logRecord.setIsOneClick(true);
        logRecord.setId(UUID.randomUUID().toString());
        String logRecordJson = JSONObject.toJSONString(logRecord);
        Long size = redisService.lRightPush(logRecordKey, logRecordJson);
        log.info("send key:{},size:{},logRecord:{}", logRecordKey, size, logRecordJson);

        String taskAppStr = JSON.toJSONString(taskApp);
        String taskAppTempKey = taskApp.getApplicationId() + CommonCode.TASK_APP_TEMP;
        redisService.set(taskAppTempKey, taskAppStr, 3600 * 24);
        if (YarnAppState.RUNNING.toString().equals(taskApp.getTaskAppState())) {
            redisService.set(taskApp.getApplicationId() + CommonCode.TASK_APP_RUNNING, taskApp.getTaskAppState(), 30);
        }

        TaskAppInfo taskAppInfo = TaskAppInfo.from(taskApp);
        taskAppInfo.setCategories(new ArrayList<>());
        DiagnoseResult diagnoseResult = new DiagnoseResult();
        diagnoseResult.setTaskAppInfo(taskAppInfo);
        diagnoseResult.setStatus(ProgressState.PROCESSING.toString());
        List<DiagnoseResult.ProcessInfo> processInfoList = new ArrayList<>();
        processInfoList.add(new DiagnoseResult.ProcessInfo(taskApp.getApplicationId() + "发送诊断中, 请稍后", 0));
        diagnoseResult.setProcessInfoList(processInfoList);
        return diagnoseResult;
    }

    private TaskApp buildTaskApp(String applicationId) throws Exception {
        TaskApplication taskApplication = null;
        YarnApp yarnApp;
        SparkApp sparkApp = null;
        TaskApplicationExample taskApplicationExample = new TaskApplicationExample();
        taskApplicationExample.createCriteria().andApplicationIdEqualTo(applicationId);
        List<TaskApplication> taskApplicationList = taskApplicationMapper.selectByExample(taskApplicationExample);
        if (taskApplicationList.size() == 0) {
            log.error("can not find this applicationId from task-application, appId:{}", applicationId);
        } else {
            taskApplication = taskApplicationList.get(0);
        }

        HashMap<String, Object> termQueryYarn = new HashMap<>();
        termQueryYarn.put("id.keyword", applicationId);
        List<YarnApp> yarnAppList = openSearchService.find(YarnApp.class, termQueryYarn, yarnAppIndex + "-*");
        if (yarnAppList.size() == 0) {
            throw new Exception(String.format("can not find this applicationId from yarn-app, appId:%s", applicationId));
        }
        yarnApp = yarnAppList.get(0);

        if (ApplicationType.SPARK.getValue().equals(yarnApp.getApplicationType())) {
            HashMap<String, Object> termQuerySpark = new HashMap<>();
            termQuerySpark.put("appId.keyword", applicationId);
            List<SparkApp> sparkAppList = openSearchService.find(SparkApp.class, termQuerySpark, sparkAppIndex + "-*");
            if (sparkAppList.size() == 0) {
                throw new Exception(String.format("can not find this applicationId from spark-app, appId:%s", applicationId));
            }
            sparkApp = sparkAppList.get(0);
        }

        TaskApp taskApp = new TaskApp();
        if (taskApplication != null) {
            BeanUtils.copyProperties(taskApplication, taskApp);
            taskApp.setExecutionDate(taskApplication.getExecuteTime());
            updateUserInfo(taskApp);
        } else {
            taskApp.setExecutionDate(new Date(yarnApp.getStartedTime()));
        }
        taskApp.updateTaskApp(yarnApp, sparkApp, redisService);

        return taskApp;
    }

    private void updateUserInfo(TaskApp taskApp) {
        TaskExample taskExample = new TaskExample();
        taskExample.createCriteria().andTaskNameEqualTo(taskApp.getTaskName()).andFlowNameEqualTo(taskApp.getFlowName()).andProjectNameEqualTo(taskApp.getProjectName());
        List<Task> tasks = taskMapper.selectByExample(taskExample);
        if (tasks.size() == 0) {
            log.warn("cant found task: {},{},{}", taskApp.getProjectName(), taskApp.getFlowName(), taskApp.getTaskName());
            return;
        }
        Task task = tasks.get(0);
        if (task != null) {
            taskApp.setTaskId(task.getId());
            taskApp.setProjectId(task.getProjectId());
            taskApp.setFlowId(task.getFlowId());
            UserExample userExample = new UserExample();
            userExample.createCriteria().andUserIdEqualTo(task.getUserId());
            List<User> users = userMapper.selectByExample(userExample);
            if (users.size() > 0) {
                User user = users.get(0);
                SimpleUser esSimpleUser = new SimpleUser();
                esSimpleUser.setUserId(user.getUserId());
                esSimpleUser.setUsername(user.getUsername());
                taskApp.setUsers(Collections.singletonList(esSimpleUser));
            }
        }
    }

    private boolean checkFinalProgressState(List<ProgressState> stateList) {
        for (ProgressState state : stateList) {
            if (state.equals(ProgressState.PROCESSING)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkFailedProgressState(List<ProgressState> stateList) {
        for (ProgressState state : stateList) {
            if (!state.equals(ProgressState.FAILED)) {
                return false;
            }
        }
        return true;
    }

    private List<TaskApp> findTaskApp(String applicationId) throws Exception {
        HashMap<String, Object> termQuery = new HashMap<>();
        termQuery.put("applicationId.keyword", applicationId);
        return openSearchService.find(TaskApp.class, termQuery, taskAppsIndex + "-*");
    }

    private void clearProgressStateCache(TaskApp taskApp) {
        List<LogType> logTypeList = registerLogTypeList(taskApp);
        for (LogType logType : logTypeList) {
            // delete all progress state cache
            redisService.del(String.format("%s:%s", taskApp.getApplicationId(), logType.getName()));
        }
    }

}
