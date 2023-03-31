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

package com.oppo.cloud.parser.service.consumer;

import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oppo.cloud.common.domain.job.LogRecord;
import com.oppo.cloud.common.service.RedisService;
import com.oppo.cloud.parser.config.CustomConfig;
import com.oppo.cloud.parser.config.ThreadPoolConfig;
import com.oppo.cloud.parser.service.job.JobManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.script.RedisScript;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;

/**
 * consume redis list data
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "custom.redisConsumer", name = "enable", havingValue = "true")
public class RedisConsumer implements CommandLineRunner {

    @Resource
    private CustomConfig config;

    @Resource
    private RedisService redisService;

    @Autowired
    private RedisScript<Object> logRecordConsumerScript;

    @Resource
    private JobManager jobManager;

    @Resource
    private ObjectMapper objectMapper;

    @Resource(name = ThreadPoolConfig.REDIS_CONSUMER_THREAD_POOL)
    private Executor redisConsumerExecutorPool;

    @PostConstruct
    void init() {
        // Reload interrupted task
        Map<Object, Object> processingMap = null;
        try {
            processingMap = redisService.hGetAll(config.getProcessingHash());
        } catch (Exception e) {
            log.error("Exception:", e);
        }
        if (processingMap != null) {
            log.info("initProcessingJobSize:{}", processingMap.size());
            processingMap.forEach((k, v) -> {
                log.info("initProcessingJobData,id:{},pushLogRecord:{}", k, v);
                redisService.lRightPush(config.getLogRecordList(), v);
                redisService.hDel(config.getProcessingHash(), k);
            });
        }
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("maxThreadPoolSize:{}", config.getMaxThreadPoolSize());
        Semaphore semaphore = new Semaphore(config.getMaxThreadPoolSize());
        while (true) {
            try {
                String msg = (String) redisService.executeScript(logRecordConsumerScript,
                        Arrays.asList(config.getLogRecordList(), config.getProcessingHash()));
                if (msg == null) {
                    Thread.sleep(5000);
                    continue;
                }
                log.info("consumeLogRecord:{}", msg);
                semaphore.acquire();
                LogRecord logRecord = JSONObject.parseObject(msg, LogRecord.class);
                redisConsumerExecutorPool.execute(() -> consume(logRecord, semaphore));
            } catch (Exception e) {
                log.error("Exception:", e);
            }
        }
    }

    private void consume(LogRecord logRecord, Semaphore semaphore) {
        try {
            jobManager.run(logRecord);
        } catch (Exception e) {
            log.error("Exception:", e);
        }finally {
            semaphore.release();
            long size = redisService.hDel(config.getProcessingHash(), logRecord.getId());
            if (size == 0) {
                log.error("delete redis cache err:{}", logRecord);
            }
        }

    }

}
