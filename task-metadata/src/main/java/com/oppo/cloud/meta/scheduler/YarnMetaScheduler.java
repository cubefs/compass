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

package com.oppo.cloud.meta.scheduler;

import com.oppo.cloud.common.service.RedisService;
import com.oppo.cloud.meta.service.ITaskSyncerMetaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.UUID;

/**
 * Synchronization of YARN Job Application List Data
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "scheduler.yarnMeta", name = "enable", havingValue = "true")
public class YarnMetaScheduler {

    /**
     * synchronize yarn app redis distributed lock
     */
    private static final String LOCK_KEY = "compass:metadata:yarn";

    @Resource
    private RedisService redisService;

    @Resource
    private RedisScript<Object> releaseLockScript;

    @Resource(name = "YarnMetaServiceImpl")
    private ITaskSyncerMetaService yarn;

    @Scheduled(cron = "${scheduler.yarnMeta.cron}")
    private void run() {
        try {
            syncer();
        } catch (Exception e) {
            log.error("Exception:", e);
        }
    }

    private void syncer() throws Exception {
        String lockValue = UUID.randomUUID().toString();
        // Only one instance of the application can run the synchronization process at the same time.
        Boolean acquire = redisService.acquireLock(LOCK_KEY, lockValue, 5L);
        if (!acquire) {
            log.info("can not get the lock: {}", LOCK_KEY);
            return;
        }
        try {
            log.info("lockKey: {}, lockValue: {}", LOCK_KEY, lockValue);
            yarn.syncer();
        } catch (Exception e) {
            log.error("Exception: ", e);
        } finally {
            Object result = redisService.executeScript(releaseLockScript, Collections.singletonList(LOCK_KEY), lockValue);
            log.info("release {}, result: {}", LOCK_KEY, result);
        }
    }

}
