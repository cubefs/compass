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

package com.oppo.cloud.common.domain.opensearch;

import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Task Application Mapping
 */
public class TaskAppMapping extends Mapping {

    public static Map<String, Object> build(boolean sourceEnabled) {
        return Stream.of(
                        new AbstractMap.SimpleEntry<>("properties", build()),
                        new AbstractMap.SimpleEntry<>("_source", enabled(sourceEnabled)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static Map<String, Object> build() {
        return Stream.of(
                        /* application ID */
                        new AbstractMap.SimpleEntry<>("applicationId", text()),
                        /* application type： Spark, MR... */
                        new AbstractMap.SimpleEntry<>("applicationType", text()),
                        /* execute user */
                        new AbstractMap.SimpleEntry<>("executeUser", text()),
                        /* queue */
                        new AbstractMap.SimpleEntry<>("queue", text()),
                        /* cluster name */
                        new AbstractMap.SimpleEntry<>("clusterName", text()),
                        /* users: [{userId: 23432, username: "someone"}] */
                        new AbstractMap.SimpleEntry<>("users", users()),
                        /* project name */
                        new AbstractMap.SimpleEntry<>("projectName", text()),
                        /* projectID */
                        new AbstractMap.SimpleEntry<>("projectId", digit("integer")),
                        /* flow name */
                        new AbstractMap.SimpleEntry<>("flowName", text()),
                        /* flow ID */
                        new AbstractMap.SimpleEntry<>("flowId", digit("integer")),
                        /* task name */
                        new AbstractMap.SimpleEntry<>("taskName", text()),
                        /* task ID */
                        new AbstractMap.SimpleEntry<>("taskId", digit("integer")),
                        /* execution date */
                        new AbstractMap.SimpleEntry<>("executionDate", date()),
                        /* start time */
                        new AbstractMap.SimpleEntry<>("startTime", date()),
                        /* finish time */
                        new AbstractMap.SimpleEntry<>("finishTime", date()),
                        /* elapsed time */
                        new AbstractMap.SimpleEntry<>("elapsedTime", digit("double")),
                        /* task app state: success、fail */
                        new AbstractMap.SimpleEntry<>("taskAppState", text()),
                        /* application memory·second */
                        new AbstractMap.SimpleEntry<>("memorySeconds", digit("double")),
                        /* application vcore·second */
                        new AbstractMap.SimpleEntry<>("vcoreSeconds", digit("double")),
                        /* am diagnostics */
                        new AbstractMap.SimpleEntry<>("diagnostics", text()),
                        /* retry times */
                        new AbstractMap.SimpleEntry<>("retryTimes", digit("integer")),
                        /* abnormal type,List:["memoryWaste", "cpuWaste", ...] */
                        new AbstractMap.SimpleEntry<>("categories", text()),
                        /* task processing status: unprocessed (0), processed (1) */
                        new AbstractMap.SimpleEntry<>("taskStatus", digit("integer")),
                        /* diagnose result: abnormal, normal */
                        new AbstractMap.SimpleEntry<>("diagnoseResult", text()),
                        /* eventLog path */
                        new AbstractMap.SimpleEntry<>("eventLogPath", text()),
                        /* yarnLog path */
                        new AbstractMap.SimpleEntry<>("yarnLogPath", text()),
                        /* amHost path */
                        new AbstractMap.SimpleEntry<>("amHost", text()),
                        /* task deletion status: not deleted (0), deleted (1) */
                        new AbstractMap.SimpleEntry<>("deleted", digit("integer")),
                        /* SparkUI */
                        new AbstractMap.SimpleEntry<>("sparkUI", text()),
                        /* create time */
                        new AbstractMap.SimpleEntry<>("createTime", date()),
                        /* update time */
                        new AbstractMap.SimpleEntry<>("updateTime", date()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
