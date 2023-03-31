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

package com.oppo.cloud.application.task;

import com.oppo.cloud.application.config.ThreadPoolConfig;
import com.oppo.cloud.application.constant.RetCode;
import com.oppo.cloud.application.domain.DelayedTaskInfo;
import com.oppo.cloud.application.domain.ParseRet;
import com.oppo.cloud.application.service.DelayedTaskService;
import com.oppo.cloud.application.service.LogParserService;
import com.oppo.cloud.common.service.RedisService;
import com.oppo.cloud.model.TaskInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * 处理日志延迟任务
 */
@Slf4j
@Component
@Configuration
@ConditionalOnProperty(prefix = "custom.delayedTask", name = "enable", havingValue = "true")
public class DelayedTask implements CommandLineRunner {

    @Value("${custom.delayedTask.queue}")
    private String queue;

    @Value("${custom.delayedTask.processing}")
    private String processingKey;

    @Value("${custom.delayedTask.delayedSeconds}")
    private Integer delayedSeconds;

    @Value("${custom.delayedTask.tryTimes}")
    private Integer  tryTimes;

    @Resource(name = ThreadPoolConfig.DELAYED_QUEUE_EXECUTOR_POOL)
    private Executor delayedQueueExecutorPool;

    @Autowired
    private DelayedTaskService delayedTaskService;

    @Autowired
    private LogParserService logParserService;

    @Autowired
    private RedisService redisService;

    @PostConstruct
    void init() {
        // 加载因重启而中断的任务
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
                redisService.zSetAdd(queue, v, System.currentTimeMillis());
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
                    Thread.sleep(delayedSeconds * 1000);
                    continue;
                }
                for (DelayedTaskInfo delayedTaskInfo : delayedTaskInfoList) {
                    delayedQueueExecutorPool.execute(() -> handleDelayTask(delayedTaskInfo));
                }
            } catch (Exception e) {
                log.error("Exception:", e);
            }
        }
    }

    public void handleDelayTask(DelayedTaskInfo delayedTaskInfo) {
        log.info("delayProcessTask:{}", delayedTaskInfo);
        TaskInstance instance = delayedTaskInfo.getTaskInstance();
        if (instance == null) {
            redisService.hDel(processingKey, delayedTaskInfo.getKey());
            return;
        }
        Map<String, String> rawData = delayedTaskInfo.getRawData();
        try {
            ParseRet parseRet = logParserService.handle(instance, rawData);
            // 重试不成功
            if (parseRet.getRetCode() != RetCode.RET_OK) {
                if (delayedTaskInfo.getTryTimes() > tryTimes) {
                    log.error("discard delay task:{}", delayedTaskInfo);
                    redisService.hDel(processingKey, delayedTaskInfo.getKey());
                    return;
                }
                delayedTaskInfo.setTryTimes(delayedTaskInfo.getTryTimes() + 1);
                delayedTaskService.pushDelayedQueue(delayedTaskInfo);
            } else {
                // 成功删除缓存
                redisService.hDel(processingKey, delayedTaskInfo.getKey());
            }
        } catch (Exception e) {
            log.error("delay task retry err: ", e);
        }
    }
}
