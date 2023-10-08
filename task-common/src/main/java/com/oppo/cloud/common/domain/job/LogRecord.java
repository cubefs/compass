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

package com.oppo.cloud.common.domain.job;

import com.oppo.cloud.common.domain.opensearch.JobAnalysis;
import com.oppo.cloud.common.domain.opensearch.TaskApp;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * LogRecord Information
 */
@Data
public class LogRecord {

    /**
     * Unique Id(uuid)
     */
    private String id;
    /**
     * Job analysis information
     */
    private JobAnalysis jobAnalysis;

    /**
     * TaskApp information: Map<applicationId, taskApp> with execution information
     */
    private Map<String, TaskApp> taskAppMap;

    /**
     * Spark Application information
     */
    private List<App> apps;

    /**
     * The LogRecord is produced by one-click.
     */
    private Boolean isOneClick = false;

    /**
     * Create time for the LogRecord
     */
    private long createTime = System.currentTimeMillis();

    /**
     * Consuming count, the `setConsumeCount` is used in src/main/resources/scripts/logRecordConsumer.lua
     */
    private Integer consumeCount = 0;

    /**
     * convert taskAppList to taskAppMap
     *
     * @param taskAppList
     */
    public void toTaskAppMap(List<TaskApp> taskAppList) {
        this.taskAppMap = taskAppList.stream().collect(Collectors.toMap(TaskApp::getApplicationId, x -> x));
    }

    /**
     * Get TaskApp From taskAppMap in LogRecord.
     *
     * @param appId
     * @return
     */
    public TaskApp getTaskApp(String appId) {
        return taskAppMap == null ? null : taskAppMap.get(appId);
    }
}
