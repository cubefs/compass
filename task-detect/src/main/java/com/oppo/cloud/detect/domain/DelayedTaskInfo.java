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

package com.oppo.cloud.detect.domain;

import com.oppo.cloud.common.domain.opensearch.JobAnalysis;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class DelayedTaskInfo {

    /**
     * 缓存key
     */
    private String key;
    /**
     * 处理延迟任务的重试次数
     */
    private Integer processRetries;
    /**
     * 延迟任务信息
     */
    private JobAnalysis jobAnalysis;
    /**
     * 已处理完成的taskApp集合
     */
    private String handledApps;

    /**
     * 处理状态(processing, success, failed)
     */
    private String processState;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 异常信息
     */
    private String exceptionInfo;

    public DelayedTaskInfo() {

    }

    public void setDelayProcessTask(JobAnalysis detectJobAnalysis, String handledApps, String exception) {
        this.key = UUID.randomUUID().toString();
        this.jobAnalysis = detectJobAnalysis;
        this.handledApps = handledApps;
        this.exceptionInfo = exception;
        this.createTime = new Date();
        this.updateTime = new Date();
        this.processRetries = 1;
    }
}
