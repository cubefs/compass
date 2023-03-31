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

import com.oppo.cloud.meta.service.IClusterConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import java.util.concurrent.TimeUnit;

/**
 * YARN、SPARK集群地址信息同步
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "scheduler.clusterMeta", name = "enable", havingValue = "true")
public class ClusterConfigScheduler {

    @Resource
    private IClusterConfigService iClusterConfigService;

    @Resource(name = "clusterMetaLock")
    private InterProcessMutex lock;

    @PostConstruct
    void init() {
        iClusterConfigService.updateClusterConfig();
    }

    @Scheduled(cron = "${scheduler.clusterMeta.cron}")
    private void run() {
        try {
            lock();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * zk锁，防止多实例同时同步数据
     */
    private void lock() throws Exception {
        if (!lock.acquire(1, TimeUnit.SECONDS)) {
            log.warn("cannot get {}", lock.getParticipantNodes());
            return;
        }
        try {
            log.info("get {}", lock.getParticipantNodes());
            iClusterConfigService.updateClusterConfig();
        } finally {
            log.info("release {}", lock.getParticipantNodes());
            lock.release();
        }
    }
}
