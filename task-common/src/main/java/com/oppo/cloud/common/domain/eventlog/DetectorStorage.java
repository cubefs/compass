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

package com.oppo.cloud.common.domain.eventlog;

import com.oppo.cloud.common.domain.eventlog.config.DetectorConfig;
import lombok.Data;

import java.util.*;

@Data
public class DetectorStorage {

    Boolean abnormal;
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
     * 任务重试次数
     */
    private Integer tryNumber;

    private String applicationId;

    private String logPath;

    private Map<String, Object> env;

    private List<DetectorResult> dataList;

    private DetectorConfig config;

    public DetectorStorage() {
    }

    public DetectorStorage(String flowName, String projectName, String taskName, Date executionTime, Integer tryNumber,
                           String applicationId, String logPath, DetectorConfig config) {
        env = new HashMap<>();
        dataList = new ArrayList<>();
        this.abnormal = false;
        this.flowName = flowName;
        this.projectName = projectName;
        this.taskName = taskName;
        this.executionTime = executionTime;
        this.tryNumber = tryNumber;
        this.applicationId = applicationId;
        this.logPath = logPath;
        this.config = config;
    }

    public void addDetectorResult(DetectorResult result) {
        this.dataList.add(result);
    }
}
