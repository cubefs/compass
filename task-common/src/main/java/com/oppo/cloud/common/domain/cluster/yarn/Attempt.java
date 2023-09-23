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

package com.oppo.cloud.common.domain.cluster.yarn;

import lombok.Data;

/**
 * sparK rest api app 重试
 */
@Data
public class Attempt {

    /**
     * 重试id
     */
    private String attemptId;
    /**
     * 开始时间
     */
    private String startTime;
    /**
     * 结束时间
     */
    private String endTime;
    /**
     * 上次更新
     */
    private String lastUpdated;
    /**
     * 运行耗时
     */
    private Integer duration;
    /**
     * spark运行用户
     */
    private String sparkUser;
    /**
     * 是否完成
     */
    private Boolean completed;
    /**
     * 版本
     */
    private String appSparkVersion;
    /**
     * 开始时间
     */
    private Long startTimeEpoch;
    /**
     * 结束时间
     */
    private Long endTimeEpoch;
    /**
     * 上次更新时间
     */
    private Long lastUpdatedEpoch;
}
