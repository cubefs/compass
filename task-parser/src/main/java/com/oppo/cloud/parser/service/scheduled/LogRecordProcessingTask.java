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

package com.oppo.cloud.parser.service.scheduled;

import com.alibaba.fastjson2.JSONObject;
import com.oppo.cloud.common.constant.Constant;
import com.oppo.cloud.common.domain.job.LogRecord;
import com.oppo.cloud.common.service.RedisService;
import com.oppo.cloud.parser.config.CustomConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import java.util.Map;

/**
 * remove task record log in redis if the task is consumed too many times exceed the limit.
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "custom.redisConsumer", name = "enable", havingValue = "true")
public class LogRecordProcessingTask {

    /**
     * The delta time between now and the time of the task consumed first time
     * when it was not finished to parse log, unit: hour.
     */
    @Value("${custom.redisConsumer.scheduled.limitTime:2}")
    private long limitTime;

    /**
     * The threshold of frequency when failed to consume task record.
     */
    @Value("${custom.redisConsumer.scheduled.limitCount:10}")
    private long limitCount;

    @Resource
    private CustomConfig customConfig;

    @Resource
    private RedisService redisService;

    @Scheduled(cron = "${custom.redisConsumer.scheduled.cron}")
    public void run() {
        Long size = redisService.hLen(customConfig.getProcessingHash());
        if (size == 0) {
            return;
        }
        Map<Object, Object> map = redisService.hGetAll(customConfig.getProcessingHash());
        if (map == null || map.size() == 0) {
            return;
        }
        log.info("processingJob size:{}", size);
        map.forEach((key, val) -> {
            LogRecord logRecord = JSONObject.parseObject((String) val, LogRecord.class);
            long spendTime = System.currentTimeMillis() - logRecord.getCreateTime();
            long limitTimesMs = limitTime * Constant.HOUR_MS;
            if (spendTime > limitTimesMs && logRecord.getConsumeCount() > limitCount) {
                log.warn("delete logRecord: {}, id: {}", customConfig.getProcessingHash(), key);
                try {
                    redisService.hDel(customConfig.getProcessingHash(), key);
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
        });
    }
}
