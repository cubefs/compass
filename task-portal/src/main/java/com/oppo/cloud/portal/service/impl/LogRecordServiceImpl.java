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

import com.alibaba.fastjson2.JSONObject;
import com.oppo.cloud.common.constant.*;
import com.oppo.cloud.common.domain.elasticsearch.JobAnalysis;
import com.oppo.cloud.common.domain.elasticsearch.TaskApp;
import com.oppo.cloud.common.domain.job.App;
import com.oppo.cloud.common.domain.job.LogInfo;
import com.oppo.cloud.common.domain.job.LogPath;
import com.oppo.cloud.common.domain.job.LogRecord;
import com.oppo.cloud.common.service.RedisService;
import com.oppo.cloud.common.util.LogPathUtil;
import com.oppo.cloud.portal.domain.app.AppDiagnosisMetadata;
import com.oppo.cloud.portal.service.LogRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class LogRecordServiceImpl implements LogRecordService {

    @Value("${custom.redis.logRecordKey}")
    private String logRecordKey;

    @Autowired
    private RedisService redisService;

    @Override
    public void reportLogRecord(AppDiagnosisMetadata appInfo) throws Exception {
        Map<String, TaskApp> taskAppMap = new HashMap<>();
        TaskApp taskApp = new TaskApp();
        taskApp.setApplicationId(appInfo.getApplicationId());
        taskApp.setApplicationType(appInfo.getApplicationType());
        taskApp.setExecuteUser(appInfo.getUser());
        taskApp.setQueue(appInfo.getQueue());
        taskApp.setClusterName(appInfo.getClusterName());
        taskApp.setExecutionDate(appInfo.getStartTime());
        taskApp.setStartTime(appInfo.getStartTime());
        taskApp.setFinishTime(appInfo.getFinishTime());
        taskApp.setElapsedTime(appInfo.getElapsedTime());
        taskApp.setMemorySeconds(appInfo.getMemorySeconds());
        taskApp.setVcoreSeconds(appInfo.getVcoreSeconds());
        taskApp.setDiagnostics(appInfo.getDiagnostics());
        taskApp.setAmHost(appInfo.getAmHost());
        taskAppMap.put(appInfo.getApplicationId(), taskApp);

        LogInfo logInfo = new LogInfo();
        Map<String, List<LogPath>> logPathMap = new HashMap<>();

        if (ApplicationType.SPARK.getValue().equals(taskApp.getApplicationType())) {
            logInfo.setLogGroup(LogGroupType.SPARK.getName());
            logPathMap.put(LogType.SPARK_EVENT.getName(), Collections.singletonList(new LogPath(ProtocolType.HDFS.getName(),
                    LogType.SPARK_EVENT.getName(), LogPathType.FILE, appInfo.getSparkEventLogFile())));
            logPathMap.put(LogType.SPARK_EXECUTOR.getName(), Collections.singletonList(new LogPath(ProtocolType.HDFS.getName(),
                    LogType.SPARK_EXECUTOR.getName(), LogPathType.DIRECTORY, appInfo.getSparkExecutorLogDirectory())));
            logInfo.setLogPathMap(logPathMap);
        }

        if (ApplicationType.MAPREDUCE.getValue().equals(appInfo.getApplicationType())) {
            logInfo.setLogGroup(LogGroupType.MAPREDUCE.getName());
            String jobId = LogPathUtil.appIdToJobId(appInfo.getApplicationId());
            logPathMap.put(LogType.MAPREDUCE_JOB_HISTORY.getName(), Collections.singletonList(
                    new LogPath(ProtocolType.HDFS.getName(), LogType.MAPREDUCE_JOB_HISTORY.getName(), LogPathType.PATTERN,
                            String.format("%s/%s*", appInfo.getMapreduceEventLogFile(), jobId))));
            logPathMap.put(LogType.MAPREDUCE_CONTAINER.getName(), Collections.singletonList(
                    new LogPath(ProtocolType.HDFS.getName(), LogType.MAPREDUCE_JOB_HISTORY.getName(), LogPathType.DIRECTORY,
                            appInfo.getMapreduceContainerLogDirectory())));
            logInfo.setLogPathMap(logPathMap);
        }

        App app = new App();
        app.setAppId(appInfo.getApplicationId());
        app.setTryNumber(1);
        app.setAmHost(appInfo.getAmHost());
        app.setLogInfoList(Collections.singletonList(logInfo));

        LogRecord logRecord = new LogRecord();
        logRecord.setId(UUID.randomUUID().toString());
        logRecord.setJobAnalysis(new JobAnalysis());
        logRecord.setTaskAppList(taskAppMap);
        logRecord.setApps(Collections.singletonList(app));

        String logRecordJson = JSONObject.toJSONString(logRecord);
        Long size = redisService.lLeftPush(logRecordKey, logRecordJson);
        log.info("reportLogRecord:{},size:{},logRecord:{}", logRecordKey, size, logRecordJson);
    }

}
