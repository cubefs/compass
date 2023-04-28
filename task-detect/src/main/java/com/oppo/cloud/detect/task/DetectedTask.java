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
import com.oppo.cloud.common.constant.SchedulerType;
import com.oppo.cloud.common.constant.TaskStateEnum;
import com.oppo.cloud.common.domain.elasticsearch.JobAnalysis;
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
 * 任务诊断
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
            // 过滤非最终状态任务数据
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
     * 判断是否最终状态
     */
    public boolean preFilter(String message) {
        // 只检测task_instance表的非删除操作
        if (message.contains("\"table\":\"task_instance\"") && !message.contains("\"eventType\":\"DELETE\"")) {
            if (message.contains("\\\"taskState\\\":\\\"success\\\"")
                    || message.contains("\\\"taskState\\\":\\\"fail\\\"")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断任务是否已将结束
     */

    public boolean judgeTaskFinished(TaskInstance taskInstance) {
        // 成功任务直接检测
        if (TaskStateEnum.success.name().equals(taskInstance.getTaskState())) {
            return true;
        }
        if (TaskStateEnum.fail.name().equals(taskInstance.getTaskState())) {
            // 手动执行的重试当成单次执行周期
            if ("manual".equals(taskInstance.getTriggerType())) {
                return true;
            } else {
                // 非手动执行的判断是否重试完成
                Integer tryNumber = TryNumberUtil.updateTryNumber(taskInstance.getRetryTimes(), schedulerType);
                log.info("schedulerType:{},{},{}, {}", schedulerType, taskInstance.getRetryTimes(), tryNumber, taskInstance.getMaxRetryTimes());
                return tryNumber.equals(taskInstance.getMaxRetryTimes());
            }
        }
        return false;
    }


    /**
     * 对每个任务进行诊断
     */
    public void detectTask(TaskInstance taskInstance) {
        if (taskInstance.getProjectName() == null || taskInstance.getFlowName() == null) {
            log.warn("instance projectName or flowName is null:{}", taskInstance);
            return;
        }
        // 过滤白名单任务
        if (blocklistService.isBlocklistTask(taskInstance.getProjectName(), taskInstance.getFlowName(),
                taskInstance.getTaskName())) {
            log.info("find blocklist task, taskInstance:{}", taskInstance);
            return;
        }
        JobAnalysis jobAnalysis = new JobAnalysis();
        TaskInstance taskInstanceSum;
        if ("manual".equals(taskInstance.getTriggerType())) {
            // 手动执行的重试当成单次执行周期
            taskInstance.setRetryTimes(0);
            taskInstance.setMaxRetryTimes(0);
            taskInstanceSum = taskInstance;
        } else {
            // 更新任务的开始/结束时间
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

        // 异常任务检测
        for (DetectService detectService : abnormalDetects) {
            try {
                detectService.detect(jobAnalysis);
            } catch (Exception e) {
                log.error("detect task failed: ", e);
            }
        }

        try {
            if (jobAnalysis.getCategories().size() == 0) {
                // 正常作业任务处理
                abnormalDetects.get(0).handleNormalJob(jobAnalysis);
            } else {
                // 异常作业任务处理
                abnormalDetects.get(0).handleAbnormalJob(jobAnalysis);
            }
        } catch (Exception e) {
            log.error("handle job failed: ", e);
        }

    }
}
