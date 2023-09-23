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

package com.oppo.cloud.meta.service;

import java.util.List;
import java.util.Map;

/**
 * YARN、SPARK集群地址配置信息
 */
public interface IClusterConfigService {

    /**
     * 获取spark history server列表
     */
    List<String> getSparkHistoryServers();

    /**
     * 获取yarn rm列表
     */
   Map<String,String> getYarnClusters();

    /**
     * 更新集群信息
     */
    void updateClusterConfig();
}
