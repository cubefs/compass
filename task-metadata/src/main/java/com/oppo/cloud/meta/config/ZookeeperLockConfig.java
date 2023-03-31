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

package com.oppo.cloud.meta.config;

import com.oppo.cloud.common.util.zookeeper.CuratorLock;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 全部zk锁管理
 */
@Slf4j
@Configuration
public class ZookeeperLockConfig extends CuratorLock {

    /**
     * YARN、SPARK集群信息同步锁
     */
    @Value("${scheduler.clusterMeta.zkPath}")
    private String clusterMetaZkPath;
    /**
     * SPARK任务元数据同步锁
     */
    @Value("${scheduler.sparkMeta.zkPath}")
    private String sparkMetaZkPath;
    /**
     * YARN任务元数据同步锁
     */
    @Value("${scheduler.yarnMeta.zkPath}")
    private String yarnMetaZkPath;
    /**
     * 创建CuratorFramework客户端
     */
    @Bean
    public CuratorFramework build(ZookeeperConfig zookeeperConfig) {
        return super.create(zookeeperConfig);
    }
    /**
     * 创建YARN、SPARK集群信息同步锁
     */
    @Bean(name = "clusterMetaLock")
    public InterProcessMutex createClusterMetaLock(CuratorFramework curatorFramework) {
        return super.create(curatorFramework, clusterMetaZkPath);
    }
    /**
     * 创建SPARK任务元数据同步锁
     */
    @Bean(name = "sparkMetaLock")
    public InterProcessMutex createSparkMetaLock(CuratorFramework curatorFramework) {
        return super.create(curatorFramework, sparkMetaZkPath);
    }
    /**
     * YARN任务元数据同步锁
     */
    @Bean(name = "yarnMetaLock")
    public InterProcessMutex createYarnMetaLock(CuratorFramework curatorFramework) {
        return super.create(curatorFramework, yarnMetaZkPath);
    }
}
