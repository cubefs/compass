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

package com.oppo.cloud.common.domain;

import lombok.Data;

/**
 * Zookeeper配置
 */
@Data
public class ZookeeperProperties {
    /**
     * 节点
     */
    private String nodes;
    /**
     * 重试次数
     */
    private int retryCount;
    /**
     * 重试间隔时间
     */
    private int elapsedTimeMs;

    /**
     * 工作空间
     */
    private String namespace;
    /**
     * Session过期时间
     */
    private int sessionTimeoutMs;
    /**
     * 连接超时时间
     */
    private int connectionTimeoutMs;
}
