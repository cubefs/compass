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

package com.oppo.cloud.parser.domain.job;

import com.oppo.cloud.common.constant.ApplicationType;
import com.oppo.cloud.common.domain.eventlog.config.DetectorConfig;
import com.oppo.cloud.parser.domain.mr.MRAppInfo;
import com.oppo.cloud.parser.utils.ReplaySparkEventLogs;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class DetectorParam {

    /**
     * 工作流名称
     */
    private String flowName;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务执行周期
     */
    private Date executionTime;

    /**
     * 任务总重试次数
     */
    private Integer tryNumber;

    /**
     * applicationId
     */
    private String appId;

    /**
     * application type
     */
    private ApplicationType appType;

    /**
     * app duration(ms)
     */
    private long appDuration;

    private String logPath;

    private DetectorConfig config;

    private boolean isOneClick;

    private ReplaySparkEventLogs replayEventLogs;

    private MRAppInfo mrAppInfo;

    public DetectorParam(String flowName, String projectName, String taskName, Date executionTime, Integer tryNumber,
                         String appId, ApplicationType appType, long appDuration, String logPath, DetectorConfig config,
                         boolean isOneClick) {
        this.flowName = flowName;
        this.projectName = projectName;
        this.taskName = taskName;
        this.executionTime = executionTime;
        this.tryNumber = tryNumber;
        this.appId = appId;
        this.appType = appType;
        this.appDuration = appDuration;
        this.logPath = logPath;
        this.config = config;
        this.isOneClick = isOneClick;
    }

}
