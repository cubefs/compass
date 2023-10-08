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

import com.alibaba.fastjson2.JSON;
import com.oppo.cloud.common.constant.TaskStateEnum;
import com.oppo.cloud.common.domain.opensearch.JobAnalysis;
import com.oppo.cloud.common.domain.syncer.TableMessage;
import com.oppo.cloud.common.util.ui.TryNumberUtil;
import com.oppo.cloud.detect.config.ThreadPoolConfig;
import com.oppo.cloud.detect.service.BlocklistService;
import com.oppo.cloud.detect.service.DetectService;
import com.oppo.cloud.detect.service.TaskInstanceService;
import com.oppo.cloud.model.TaskInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Detected Task Service
 */
@Component
@Slf4j
public class DetectedTask {

    @Value("${custom.schedulerType}")
    private String schedulerType;
    @Resource
    private List<DetectService> abnormalDetects;

    @Resource(name = ThreadPoolConfig.DETECT_EXECUTOR_POOL)
    private Executor detectExecutorPool;

    @Autowired
    private TaskInstanceService taskInstanceService;

    @Autowired
    private BlocklistService blocklistService;

    @KafkaListener(topics = "${custom.kafka.consumer.topic-name}", groupId = "${custom.kafka.consumer.group-id}", autoStartup = "${custom.kafka.consumer.auto.start}")
    public void consumerTask(@Payload List<String> tableChangeMessages, Acknowledgment ack) {
        for (String message : tableChangeMessages) {
            // Filter non-final state task data
            if (preFilter(message)) {
                log.info("message:{}", message);
                TableMessage tableMessage;
                try {
                    tableMessage = JSON.parseObject(message, TableMessage.class);
                } catch (Exception e) {
                    log.error("parse kafka message failed, error msg:{}, kafka message:{}", e.getMessage(), message);
                    continue;
                }
                TaskInstance taskInstance;
                try {
                    taskInstance = JSON.parseObject(tableMessage.getBody(), TaskInstance.class);
                } catch (Exception e) {
                    log.error("parse taskInstance message failed, error msg:{}, kafka message:{}", e.getMessage(),
                            tableMessage.getBody());
                    continue;
                }
                if (judgeTaskFinished(taskInstance)) {
                    detectExecutorPool.execute(() -> detectTask(taskInstance));
                }
            }
        }
        ack.acknowledge();
    }

    /**
     * Check if it is the final state
     */
    public boolean preFilter(String message) {
        // Only detect non-deletion operations in the task_instance table.
        if (message.contains("\"table\":\"task_instance\"") && !message.contains("\"eventType\":\"DELETE\"")) {
            if (message.contains("\\\"taskState\\\":\\\"success\\\"")
                    || message.contains("\\\"taskState\\\":\\\"fail\\\"")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determine whether the task has already ended.
     */

    public boolean judgeTaskFinished(TaskInstance taskInstance) {
        // Successful tasks are detected directly.
        if (TaskStateEnum.success.name().equals(taskInstance.getTaskState())) {
            return true;
        }
        if (TaskStateEnum.fail.name().equals(taskInstance.getTaskState())) {
            // Manually executed retries are treated as a single execution cycle.
            if ("manual".equals(taskInstance.getTriggerType())) {
                return true;
            } else {
                // Non-manual executions are judged whether the retries are completed.
                Integer tryNumber = TryNumberUtil.updateTryNumber(taskInstance.getRetryTimes(), schedulerType);
                log.info("schedulerType:{},{},{}, {}", schedulerType, taskInstance.getRetryTimes(), tryNumber, taskInstance.getMaxRetryTimes());
                return tryNumber.equals(taskInstance.getMaxRetryTimes());
            }
        }
        return false;
    }


    /**
     * Diagnose each task.
     */
    public void detectTask(TaskInstance taskInstance) {
        if (taskInstance.getProjectName() == null || taskInstance.getFlowName() == null) {
            log.warn("instance projectName or flowName is null:{}", taskInstance);
            return;
        }
        // Filter out whitelisted tasks.
        if (blocklistService.isBlocklistTask(taskInstance.getProjectName(), taskInstance.getFlowName(),
                taskInstance.getTaskName())) {
            log.info("find blocklist task, taskInstance:{}", taskInstance);
            return;
        }
        JobAnalysis jobAnalysis = new JobAnalysis();
        TaskInstance taskInstanceSum;
        if ("manual".equals(taskInstance.getTriggerType())) {
            // Manually executed retries are treated as a single execution cycle.
            taskInstance.setRetryTimes(0);
            taskInstance.setMaxRetryTimes(0);
            taskInstanceSum = taskInstance;
        } else {
            // Update the start/end time of the task.
            taskInstanceSum = taskInstanceService.searchTaskSum(taskInstance.getProjectName(),
                    taskInstance.getFlowName(), taskInstance.getTaskName(), taskInstance.getExecutionTime());
        }
        try {
            BeanUtils.copyProperties(taskInstanceSum, jobAnalysis);
        } catch (Exception e) {
            log.error("taskInstanceSum:{}, taskInstance:{}, exception:{}", taskInstanceSum, taskInstance, e.getMessage());
            return;
        }
        jobAnalysis.setExecutionDate(taskInstanceSum.getExecutionTime());
        jobAnalysis.setDuration((double) (taskInstanceSum.getEndTime().getTime() / 1000
                - taskInstanceSum.getStartTime().getTime() / 1000));
        jobAnalysis.setCategories(new ArrayList<>());

        jobAnalysis.setRetryTimes(TryNumberUtil.updateTryNumber(jobAnalysis.getRetryTimes(),schedulerType));

        // Exception task detection.
        for (DetectService detectService : abnormalDetects) {
            try {
                detectService.detect(jobAnalysis);
            } catch (Exception e) {
                log.error("detect task failed: ", e);
            }
        }

        try {
            if (jobAnalysis.getCategories().size() == 0) {
                // Normal job task processing.
                abnormalDetects.get(0).handleNormalJob(jobAnalysis);
            } else {
                // Exception job task processing.
                abnormalDetects.get(0).handleAbnormalJob(jobAnalysis);
            }
        } catch (Exception e) {
            log.error("handle job failed: ", e);
        }

    }
}
