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

package com.oppo.cloud.detect.detector;

import com.alibaba.fastjson2.JSONObject;
import com.oppo.cloud.common.domain.opensearch.JobAnalysis;
import com.oppo.cloud.common.domain.opensearch.SimpleUser;
import com.oppo.cloud.common.domain.job.App;
import com.oppo.cloud.common.domain.job.LogRecord;
import com.oppo.cloud.common.service.RedisService;
import com.oppo.cloud.common.util.DateUtil;
import com.oppo.cloud.detect.domain.AbnormalTaskAppInfo;
import com.oppo.cloud.detect.service.*;
import com.oppo.cloud.detect.util.DetectorUtil;
import com.oppo.cloud.mapper.*;
import com.oppo.cloud.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;

/**
 * Job Detection Service
 */
@Slf4j
public abstract class DetectServiceImpl implements DetectService {

    @Autowired
    private OpenSearchService openSearchService;

    @Value("${custom.opensearch.job-index}")
    private String jobIndex;

    @Autowired
    public TaskService taskService;

    @Autowired
    private UserInfoMapper userMapper;

    @Autowired
    private LogRecordService logRecordService;

    @Autowired
    private TaskAppService taskAppService;

    @Autowired
    private DelayedTaskService delayTaskService;

    @Autowired
    private AbnormalJobService abnormalJobService;

    @Autowired
    private RedisService redisService;

    @Autowired
    public TaskInstanceService taskInstanceService;

    @Autowired
    private JobInstanceService jobInstanceService;

    /**
     * Parsing message transmission through a Redis queue.
     */
    @Value("${custom.redis.logRecord}")
    private String logRecordQueue;

    /**
     * Task diagnosis
     */
    @Override
    public abstract void detect(JobAnalysis detectJobAnalysis) throws Exception;

    /**
     * Normal task processing.
     */
    @Override
    public void handleNormalJob(JobAnalysis detectJobAnalysis) throws Exception {
        // Update user information.
        updateUserInfo(detectJobAnalysis);
        // Query the appIds under this task.
        AbnormalTaskAppInfo abnormalTaskAppInfo = taskAppService.getAbnormalTaskAppsInfo(detectJobAnalysis, null);
        if (!"".equals(abnormalTaskAppInfo.getExceptionInfo())) {
            // Send after constructed it.
            delayTaskService.pushDelayedQueue(detectJobAnalysis, abnormalTaskAppInfo.getHandleApps(),
                    abnormalTaskAppInfo.getExceptionInfo());
            return;
        }
        // An appId is required for engine-level diagnostic.
        if (abnormalTaskAppInfo.getTaskAppList().size() != 0) {
            // Generate parsing log message body logRecord.
            abnormalJobService.updateResource(detectJobAnalysis, abnormalTaskAppInfo.getTaskAppList());
            LogRecord logRecord = this.genLogRecord(abnormalTaskAppInfo, detectJobAnalysis);
            this.sendLogRecordMsg(logRecord);
        }
        // Save jobInstance information.
        jobInstanceService.insertOrUpdate(detectJobAnalysis);
    }

    /**
     * Exceptional task processing
     */
    @Override
    public void handleAbnormalJob(JobAnalysis detectJobAnalysis) throws Exception {
        // Update user information.
        updateUserInfo(detectJobAnalysis);
        sendAbnormalJobApp(detectJobAnalysis);
        // Save abnormal tasks.
        this.addOrUpdate(detectJobAnalysis);
    }


    public void sendAbnormalJobApp(JobAnalysis detectJobAnalysis) throws Exception {
        // Query the appIds under this task.
        AbnormalTaskAppInfo abnormalTaskAppInfo = taskAppService.getAbnormalTaskAppsInfo(detectJobAnalysis, null);
        if (abnormalTaskAppInfo.getTaskAppList().size() != 0) {
            taskAppService.insertTaskApps(abnormalTaskAppInfo.getTaskAppList());
            // Update vcoreSeconds and memorySeconds
            abnormalJobService.updateResource(detectJobAnalysis, abnormalTaskAppInfo.getTaskAppList());
        }
        // Generate parsing log message body logRecord.
        LogRecord logRecord = this.genLogRecord(abnormalTaskAppInfo, detectJobAnalysis);

        // Push to the delay queue and retry if there is exception existing.
        if (!"".equals(abnormalTaskAppInfo.getExceptionInfo())) {
            delayTaskService.pushDelayedQueue(detectJobAnalysis, abnormalTaskAppInfo.getHandleApps(),
                    abnormalTaskAppInfo.getExceptionInfo());
        }
        // There is no message to send.
        if (logRecord.getApps().size() == 0) {
            return;
        }
        // Send a parsing message.
        this.sendLogRecordMsg(logRecord);
        // Save jobInstance information.
        jobInstanceService.insertOrUpdate(detectJobAnalysis);
    }


    /**
     * Generate LogRecord
     */
    public LogRecord genLogRecord(AbnormalTaskAppInfo abnormalTaskAppInfo, JobAnalysis detectJobAnalysis) {
        LogRecord logRecord = new LogRecord();
        logRecord.setId(UUID.randomUUID().toString());
        logRecord.setIsOneClick(false);
        logRecord.setJobAnalysis(detectJobAnalysis);
        logRecord.toTaskAppMap(abnormalTaskAppInfo.getTaskAppList());
        List<App> appLogPath = logRecordService.getAppLog(abnormalTaskAppInfo.getTaskAppList());
        List<App> schedulerLogApp = logRecordService.getSchedulerLog(detectJobAnalysis);
        appLogPath.addAll(schedulerLogApp);
        logRecord.setApps(appLogPath);
        if (schedulerLogApp.size() != 0) {
            // 更新已处理的事件信息【记录调度日志已成功发送】
            abnormalTaskAppInfo.setHandleApps(abnormalTaskAppInfo.getHandleApps() + "scheduler" + ";");
        }
        return logRecord;
    }

    /**
     * Send LogRecord
     */
    public void sendLogRecordMsg(LogRecord logRecord) {
        Long size = redisService.lLeftPush(logRecordQueue, JSONObject.toJSONString(logRecord));
        log.info("send logRecord: key:{}, size:{}, data:{}", logRecordQueue, size, logRecord);
    }

    /**
     * Save exception task data
     */
    public void addOrUpdate(JobAnalysis detectJobAnalysis) throws Exception {
        JobAnalysis esJobAnalysis = abnormalJobService.searchJob(detectJobAnalysis);
        if (esJobAnalysis != null) {
            // Update operation
            esJobAnalysis.getCategories().addAll(detectJobAnalysis.getCategories());
            if (Strings.isNotBlank(detectJobAnalysis.getSuccessExecutionDay())) {
                esJobAnalysis.setSuccessExecutionDay(detectJobAnalysis.getSuccessExecutionDay());
            }
            if (Strings.isBlank(detectJobAnalysis.getSuccessDays())) {
                esJobAnalysis.setSuccessDays(detectJobAnalysis.getSuccessDays());
            }
            if (Strings.isBlank(detectJobAnalysis.getDurationBaseline())) {
                esJobAnalysis.setDurationBaseline(detectJobAnalysis.getDurationBaseline());
            }
            if (Strings.isBlank(detectJobAnalysis.getEndTimeBaseline())) {
                esJobAnalysis.setEndTimeBaseline(detectJobAnalysis.getEndTimeBaseline());
            }
            esJobAnalysis.setUpdateTime(new Date());
            openSearchService.insertOrUpDate(esJobAnalysis.getIndex(), esJobAnalysis.getDocId(),
                    esJobAnalysis.genDoc());
        } else {
            // Add operation
            detectJobAnalysis.setCreateTime(new Date());
            detectJobAnalysis.setUpdateTime(new Date());
            String index = detectJobAnalysis.genIndex(jobIndex);
            String docId = detectJobAnalysis.genDocId();
            openSearchService.insertOrUpDate(index, docId, detectJobAnalysis.genDoc());
            // Record index information and docId
            detectJobAnalysis.setIndex(index);
            detectJobAnalysis.setDocId(docId);
        }
    }


    /**
     * Update user information of the task
     */
    public void updateUserInfo(JobAnalysis detectJobAnalysis) {
        Task task = taskService.getTask(detectJobAnalysis.getProjectName(), detectJobAnalysis.getFlowName(),
                detectJobAnalysis.getTaskName());
        if (task == null) {
            log.error("get task null:{}", detectJobAnalysis);
            return;
        }
        detectJobAnalysis.setTaskId(task.getId());
        detectJobAnalysis.setProjectId(task.getProjectId());
        detectJobAnalysis.setFlowId(task.getFlowId());
        UserInfoExample userExample = new UserInfoExample();
        userExample.createCriteria().andUserIdEqualTo(task.getUserId());
        List<UserInfo> users = userMapper.selectByExample(userExample);
        if (users.size() > 0) {
            UserInfo user = users.get(0);
            SimpleUser simpleUser = new SimpleUser();
            simpleUser.setUserId(user.getUserId());
            simpleUser.setUsername(user.getUsername());
            detectJobAnalysis.setUsers(Collections.singletonList(simpleUser));
        }

    }

    public double[] getEndTimeBaseline(JobAnalysis detectJobAnalysis) throws Exception {
        Date startTime = DateUtil.getOffsetDate(detectJobAnalysis.getExecutionDate(), -30);
        // Get data within the last month
        List<Double> relativeEndDateHistory = taskInstanceService.searchTaskRelativeEndTime(
                detectJobAnalysis.getProjectName(), detectJobAnalysis.getFlowName(),
                detectJobAnalysis.getTaskName(), detectJobAnalysis.getExecutionDate(), startTime, 20);
        // If the sample value is less than 10, do not perform anomaly detection
        if (relativeEndDateHistory.size() < 10) {
            return null;
        }
        Double[] relativeEndDate = relativeEndDateHistory.toArray(new Double[0]);
        long executionTimestamp = detectJobAnalysis.getExecutionDate().getTime() / 1000;
        double[] relativeMedian = DetectorUtil.boxplotValue(relativeEndDate);
        double relativeEndDateStart = relativeMedian[0];
        double relativeEndDateEnd = relativeMedian[4];
        double normalEndDateBegin = relativeEndDateStart + executionTimestamp;
        double normalEndDateEnd = relativeEndDateEnd + executionTimestamp;
        return new double[]{normalEndDateBegin, normalEndDateEnd};
    }

    public double[] getDurationBaseline(JobAnalysis detectJobAnalysis) throws Exception {
        Date startTime = DateUtil.getOffsetDate(detectJobAnalysis.getExecutionDate(), 30);
        // Query the historical data of the last month
        List<Double> durationHistory = taskInstanceService.searchTaskDurationHistory(detectJobAnalysis.getProjectName(),
                detectJobAnalysis.getFlowName(), detectJobAnalysis.getTaskName(), detectJobAnalysis.getExecutionDate(),
                startTime, 20);
        // If the sample value is less than 10, do not perform anomaly detection
        if (durationHistory.size() < 10) {
            return null;
        }
        Double[] durationData = durationHistory.toArray(new Double[0]);
        // Box plot method
        double[] durationBeginAndEnd = DetectorUtil.boxplotValue(durationData);
        // Extreme outliers
        double normalDurationBegin = durationBeginAndEnd[0];
        double normalDurationEnd = durationBeginAndEnd[4];
        return new double[]{normalDurationBegin, normalDurationEnd};
    }
}
