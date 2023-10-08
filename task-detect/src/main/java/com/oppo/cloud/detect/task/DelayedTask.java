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

package com.oppo.cloud.detect.task;

import com.alibaba.fastjson2.JSONObject;
import com.oppo.cloud.common.domain.opensearch.JobAnalysis;
import com.oppo.cloud.common.domain.opensearch.TaskApp;
import com.oppo.cloud.common.domain.job.App;
import com.oppo.cloud.common.domain.job.LogRecord;
import com.oppo.cloud.common.service.RedisService;
import com.oppo.cloud.detect.config.ThreadPoolConfig;
import com.oppo.cloud.detect.domain.AbnormalTaskAppInfo;
import com.oppo.cloud.detect.domain.DelayedTaskInfo;
import com.oppo.cloud.detect.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.Executor;

/**
 * Delayed task scheduled scanning and processing service.
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "custom.delayedTaskQueue", name = "enable", havingValue = "true")
public class DelayedTask implements CommandLineRunner {

    @Value("${custom.redis.logRecord}")
    private String logRecordQueue;

    @Value("${custom.redis.delayedQueue}")
    private String delayedQueue;

    @Value("${custom.redis.processing}")
    private String processingKey;

    @Value("${custom.delayedTaskQueue.delayedSeconds}")
    private Integer delaySeconds;

    @Value("${custom.delayedTaskQueue.tryTimes}")
    private Integer tryTimes;

    @Resource(name = ThreadPoolConfig.DELAY_QUEUE_EXECUTOR_POOL)
    private Executor delayQueueExecutorPool;

    @Autowired
    private DelayedTaskService delayedTaskService;

    @Autowired
    private LogRecordService logRecordService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private TaskAppService taskAppService;

    @Autowired
    private AbnormalJobService abnormalJobService;

    @Autowired
    private JobInstanceService jobInstanceService;


    @PostConstruct
    void init() {
        // Resume tasks interrupted by restart.
        Map<Object, Object> processingMap = null;
        try {
            processingMap = redisService.hGetAll(processingKey);
        } catch (Exception e) {
            log.error("get processing key err:", e);
        }
        if (processingMap != null) {
            log.info("initProcessingTaskSize:{}", processingMap.size());
            processingMap.forEach((k, v) -> {
                log.info("initProcessingTaskData,k:{},v:{}", k, v);
                redisService.zSetAdd(delayedQueue, v, System.currentTimeMillis());
                redisService.hDel(processingKey, k);
            });
        }
    }

    @Override
    public void run(String... args) throws Exception {
        while (true) {
            try {
                List<DelayedTaskInfo> delayedTaskInfoList = delayedTaskService.getDelayedTasks();
                if (delayedTaskInfoList == null) {
                    Thread.sleep(delaySeconds * 1000);
                    continue;
                }
                for (DelayedTaskInfo delayedTaskInfo : delayedTaskInfoList) {
                    delayQueueExecutorPool.execute(() -> handleDelayTask(delayedTaskInfo));
                }
            } catch (Exception e) {
                log.error("Exception:", e);
            }
        }
    }

    public void handleDelayTask(DelayedTaskInfo delayedTaskInfo) {
        log.info("delayProcessTask:{}", delayedTaskInfo);
        try {
            if (delayedTaskInfo.getJobAnalysis().getCategories().size() == 0) {
                dealWithNormalTask(delayedTaskInfo);
            } else {
                dealWithAbnormalTask(delayedTaskInfo);
            }
        } catch (Exception e) {
            log.error("dealWithDelayTask failed, msg: ", e);
        }
    }

    /**
     * Processing retries for failed tasks
     */
    public void dealWithAbnormalTask(DelayedTaskInfo delayedTaskInfo) throws Exception {
        // If the number of retries exceeds the limit, give up the retry and save the relevant appId.
        // TODO: Possibly send AM logs directly if Spark tasks have not started.
        if (delayedTaskInfo.getProcessRetries() >= tryTimes) {
            try {
                saveAllAbnormalTaskApp(delayedTaskInfo);
            } catch (Exception e) {
                log.error("saveAllAbnormalTaskApp failed ", e);
            }
            redisService.hDel(processingKey, delayedTaskInfo.getKey());
            log.error("delay task retry failed:{}", delayedTaskInfo);
            return;
        }

        AbnormalTaskAppInfo abnormalTaskAppInfo =
                taskAppService.getAbnormalTaskAppsInfo(delayedTaskInfo.getJobAnalysis(),
                        delayedTaskInfo.getHandledApps());

        // save appIds
        if (abnormalTaskAppInfo.getTaskAppList().size() != 0) {
            taskAppService.insertTaskApps(abnormalTaskAppInfo.getTaskAppList());
            abnormalJobService.updateResource(delayedTaskInfo.getJobAnalysis(), abnormalTaskAppInfo.getTaskAppList());
            abnormalJobService.insertOrUpdate(delayedTaskInfo.getJobAnalysis());
            jobInstanceService.insertOrUpdate(delayedTaskInfo.getJobAnalysis());
        }

        LogRecord logRecord = buildLogRecord(delayedTaskInfo, abnormalTaskAppInfo);

        if (!delayedTaskInfo.getHandledApps().contains("scheduler")) {
            // The first sending needs to be accompanied by the scheduling log.
            List<App> schedulerLogApps = logRecordService.getSchedulerLog(delayedTaskInfo.getJobAnalysis());
            if (schedulerLogApps.size() != 0) {
                logRecord.getApps().addAll(schedulerLogApps);
                delayedTaskInfo.setHandledApps(delayedTaskInfo.getHandledApps() + "scheduler;");
            }
        }
        // Process delayed task information.
        handleTryTask(abnormalTaskAppInfo, delayedTaskInfo);

        if (logRecord.getApps().size() == 0) {
            return;
        }
        // Send a message for log analysis
        String logRecordJson = JSONObject.toJSONString(logRecord);
        Long size = redisService.lLeftPush(logRecordQueue, logRecordJson);
        log.info("pushLogRecord: key:{}, size:{}, data:{}", logRecordQueue, size, logRecordJson);
    }

    /**
     * Handling of retries for regular tasks
     */
    public void dealWithNormalTask(DelayedTaskInfo delayedTaskInfo) throws Exception {
        // If the retry count is greater than the specified limit, give up retrying.
        if (delayedTaskInfo.getProcessRetries() >= tryTimes) {
            log.warn("discard retry task:{}", delayedTaskInfo);
            redisService.hDel(processingKey, delayedTaskInfo.getKey());
            return;
        }

        AbnormalTaskAppInfo abnormalTaskAppInfo = taskAppService
                .getAbnormalTaskAppsInfo(delayedTaskInfo.getJobAnalysis(), delayedTaskInfo.getHandledApps());

        if (!"".equals(abnormalTaskAppInfo.getExceptionInfo())) {
            // Construct the complete appId information before sending, or retain the fallback information for the last retry.
            if (delayedTaskInfo.getProcessRetries() != tryTimes - 1) {
                delayedTaskInfo.setProcessRetries(delayedTaskInfo.getProcessRetries() + 1);
                delayedTaskService.rePushDelayedQueue(delayedTaskInfo);
                return;
            }
        }
        if (abnormalTaskAppInfo.getTaskAppList().size() != 0) {
            // Update job memorySeconds and vcoreSeconds
            abnormalJobService.updateResource(delayedTaskInfo.getJobAnalysis(), abnormalTaskAppInfo.getTaskAppList());
        }
        // Save job instance
        jobInstanceService.insertOrUpdate(delayedTaskInfo.getJobAnalysis());

        LogRecord logRecord = buildLogRecord(delayedTaskInfo, abnormalTaskAppInfo);

        boolean firstSend = StringUtils.isEmpty(delayedTaskInfo.getHandledApps());
        if (firstSend) {
            // The first sending needs to be accompanied by the scheduling log.
            List<App> schedulerLogApps = logRecordService.getSchedulerLog(delayedTaskInfo.getJobAnalysis());
            if (schedulerLogApps.size() != 0) {
                logRecord.getApps().addAll(schedulerLogApps);
                delayedTaskInfo.setHandledApps(delayedTaskInfo.getHandledApps() + "scheduler;");
            } else {
                log.warn("cant found schedulerLogApps");
            }
        }

        // Process delayed task information.
        handleTryTask(abnormalTaskAppInfo, delayedTaskInfo);

        if (logRecord.getApps().size() == 0) {
            return;
        }
        String logRecordJson = JSONObject.toJSONString(logRecord);
        Long size = redisService.lLeftPush(logRecordQueue, logRecordJson);
        log.info("pushLogRecord: key:{}, size:{}, data:{}", logRecordQueue, size, logRecordJson);
    }

    /**
     * Process delayed task information.
     */
    public void handleTryTask(AbnormalTaskAppInfo abnormalTaskAppInfo, DelayedTaskInfo delayedTaskInfo) {
        if ("".equals(abnormalTaskAppInfo.getExceptionInfo())) {
            delayedTaskInfo.setHandledApps(delayedTaskInfo.getHandledApps() + abnormalTaskAppInfo.getHandleApps());
            redisService.hDel(processingKey, delayedTaskInfo.getKey());
            log.info("retry delay task success:{}", delayedTaskInfo);
        } else {
            delayedTaskInfo.setExceptionInfo(abnormalTaskAppInfo.getExceptionInfo());
            delayedTaskInfo.setHandledApps(delayedTaskInfo.getHandledApps() + abnormalTaskAppInfo.getHandleApps());
            delayedTaskInfo.setProcessRetries(delayedTaskInfo.getProcessRetries() + 1);
            delayedTaskInfo.setUpdateTime(new Date());
            delayedTaskService.rePushDelayedQueue(delayedTaskInfo);

        }
    }

    /**
     * Construct a LogRecord.
     */

    public LogRecord buildLogRecord(DelayedTaskInfo delayedTaskInfo, AbnormalTaskAppInfo abnormalTaskAppInfo) {
        LogRecord logRecord = new LogRecord();
        logRecord.setId(UUID.randomUUID().toString());
        logRecord.setJobAnalysis(delayedTaskInfo.getJobAnalysis());
        logRecord.toTaskAppMap(abnormalTaskAppInfo.getTaskAppList());
        List<App> appLogPath = logRecordService.getAppLog(abnormalTaskAppInfo.getTaskAppList());
        logRecord.setApps(appLogPath);
        return logRecord;
    }

    /**
     * Finally, check and save all appIds.
     */
    public void saveAllAbnormalTaskApp(DelayedTaskInfo delayedTaskInfo) throws Exception {
        Map<Integer, List<TaskApp>> allAbnormalTaskApp =
                taskAppService.getAbnormalTaskApps(delayedTaskInfo.getJobAnalysis());
        List<TaskApp> needInsertTaskApp = new ArrayList<>();
        for (int i = 0; i <= delayedTaskInfo.getJobAnalysis().getRetryTimes(); i++) {
            List<TaskApp> abnormalTaskAppList = allAbnormalTaskApp.get(i);
            for (TaskApp taskApp : abnormalTaskAppList) {
                if (taskApp.getApplicationId() != null) {
                    if (!delayedTaskInfo.getHandledApps().contains(taskApp.getApplicationId())) {
                        // Already processed appId.
                        needInsertTaskApp.add(taskApp);
                    }
                }
            }
        }
        if (needInsertTaskApp.size() != 0) {
            taskAppService.insertTaskApps(needInsertTaskApp);
            abnormalJobService.updateResource(delayedTaskInfo.getJobAnalysis(), needInsertTaskApp);
            JobAnalysis jobAnalysis = delayedTaskInfo.getJobAnalysis();
            abnormalJobService.insertOrUpdate(jobAnalysis);
            jobInstanceService.insertOrUpdate(delayedTaskInfo.getJobAnalysis());
        }
    }


}
