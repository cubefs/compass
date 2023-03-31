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

package com.oppo.cloud.common.util.zookeeper;

import com.oppo.cloud.common.domain.ZookeeperProperties;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * zk客户端配置
 */
public class ZookeeperCuratorClient {

    public CuratorFramework create(ZookeeperProperties properties) {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(properties.getElapsedTimeMs(),
                properties.getRetryCount());
        CuratorFramework client = CuratorFrameworkFactory
                .builder()
                .connectString(properties.getNodes())
                .retryPolicy(retryPolicy)
                .namespace(properties.getNamespace())
                .sessionTimeoutMs(properties.getSessionTimeoutMs())
                .build();
        client.start();
        return client;
    }
}
