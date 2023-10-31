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
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FlinkTaskAnalysisMapping extends Mapping {

    public static Map<String, Object> build(boolean sourceEnabled) {
        return Stream.of(
                new AbstractMap.SimpleEntry<>("properties", build()),
                new AbstractMap.SimpleEntry<>("_source", enabled(sourceEnabled))
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static Map<String, Object> advice() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("ruleName", text());
        properties.put("ruleAlias", text());
        properties.put("ruleCode", digit("integer"));
        properties.put("hasAdvice", digit("integer"));
        properties.put("description", text());
        return Stream.of(
                new AbstractMap.SimpleEntry<>("properties", properties)
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static Map<String, Object> build() {
        return Stream.of(
                /* Flink task app Id */
                new AbstractMap.SimpleEntry<>("flinkTaskAppId", digit("integer")),
                /* Users : [{userId: 23432, username: "someone"}] */
                new AbstractMap.SimpleEntry<>("users", users()),
                /* Project name */
                new AbstractMap.SimpleEntry<>("projectName", text()),
                /* Project ID */
                new AbstractMap.SimpleEntry<>("projectId", digit("integer")),
                /* Flow name */
                new AbstractMap.SimpleEntry<>("flowName", text()),
                /* Flow id */
                new AbstractMap.SimpleEntry<>("flowId", digit("integer")),
                /* Task name */
                new AbstractMap.SimpleEntry<>("taskName", text()),
                /* Task ID */
                new AbstractMap.SimpleEntry<>("taskId", digit("integer")),
                /* yarn applicationId */
                new AbstractMap.SimpleEntry<>("applicationId", text()),
                /* flink track url */
                new AbstractMap.SimpleEntry<>("flinkTrackUrl", text()),
                /* Allocated memory (Unit: MB) */
                new AbstractMap.SimpleEntry<>("allocatedMB", digit("long")),
                /* Allocated vcores */
                new AbstractMap.SimpleEntry<>("allocatedVcores", digit("integer")),
                /* Allocated running containers */
                new AbstractMap.SimpleEntry<>("runningContainers", digit("integer")),
                /* Engine ? */
                new AbstractMap.SimpleEntry<>("engineType", text()),
                /* Execution Date */
                new AbstractMap.SimpleEntry<>("executionDate", date()),
                /* Running time */
                new AbstractMap.SimpleEntry<>("duration", digit("double")),
                /* Start time */
                new AbstractMap.SimpleEntry<>("startTime", date()),
                /* End time */
                new AbstractMap.SimpleEntry<>("endTime", date()),
                /* CPU consumption (vcore-seconds) */
                new AbstractMap.SimpleEntry<>("vcoreSeconds", digit("float")),
                /* Memory consumption (GB-seconds) */
                new AbstractMap.SimpleEntry<>("memorySeconds", digit("float")),
                /* Queue */
                new AbstractMap.SimpleEntry<>("queue", text()),
                /* Cluster name */
                new AbstractMap.SimpleEntry<>("clusterName", text()),
                /* Times of retries */
                new AbstractMap.SimpleEntry<>("retryTimes", digit("integer")),
                /* Execute User */
                new AbstractMap.SimpleEntry<>("executeUser", text()),
                /* Yarn diagnosis information */
                new AbstractMap.SimpleEntry<>("diagnosis", text()),
                /* Parallel */
                new AbstractMap.SimpleEntry<>("parallel", digit("integer")),
                /* flink slot */
                new AbstractMap.SimpleEntry<>("tmSlot", digit("integer")),
                /* flink task manager core */
                new AbstractMap.SimpleEntry<>("tmCore", digit("integer")),
                /* flink task manager memory */
                new AbstractMap.SimpleEntry<>("tmMemory", digit("integer")),
                /* flink job manager memory */
                new AbstractMap.SimpleEntry<>("jmMemory", digit("integer")),
                /* flink task manager num */
                new AbstractMap.SimpleEntry<>("tmNum", digit("integer")),
                /* flink job name */
                new AbstractMap.SimpleEntry<>("jobName", text()),
                /*  Start time of diagnosis */
                new AbstractMap.SimpleEntry<>("diagnosisStartTime", date()),
                /*  End time of diagnosis */
                new AbstractMap.SimpleEntry<>("diagnosisEndTime", date()),
                /* Resource diagnosis type:
                  - 0: Expand CPU
                  - 1: Expand Memory,
                  - 2: Reduce CPU,
                  - 3: Reduce Memory,
                  - 4: Running abnormally */
                new AbstractMap.SimpleEntry<>("diagnosisResourceType", text()),
                /* Diagnosis source:
                  - 0: Midnight scheduled task
                  - 1: Diagnosis after task going online
                  - 2: Real-time diagnosis */
                new AbstractMap.SimpleEntry<>("diagnosisSource", digit("integer")),
                /* Advice parallel after diagnosis */
                new AbstractMap.SimpleEntry<>("diagnosisParallel", digit("integer")),
                /* Advice JobManager memory(Unit: MB) after diagnosis */
                new AbstractMap.SimpleEntry<>("diagnosisJmMemory", digit("integer")),
                /* Advice TaskManager memory(Unit: MB) after diagnosis */
                new AbstractMap.SimpleEntry<>("diagnosisTmMemory", digit("integer")),
                /* Advice Task slots after diagnosis */
                new AbstractMap.SimpleEntry<>("diagnosisTmSlotNum", digit("integer")),
                /* Advice TaskManager core number after diagnosis */
                new AbstractMap.SimpleEntry<>("diagnosisTmCoreNum", digit("integer")),
                /* Advice TaskManager number after diagnosis */
                new AbstractMap.SimpleEntry<>("diagnosisTmNum", digit("integer")),
                /* Diagnosis Type: [Low memory usage][High CPU peak utilization rate] */
                new AbstractMap.SimpleEntry<>("diagnosisTypes", text()),
                /* Processing State: (processing, success, failed) */
                new AbstractMap.SimpleEntry<>("processState", text()),
                /* Diagnosis advice：[{ruleName, ruleType, hasAdvice, description}, {...}] */
                new AbstractMap.SimpleEntry<>("advices", advice()),
                /* Optimizable number of cores */
                new AbstractMap.SimpleEntry<>("cutCoreNum", digit("long")),
                /* Total number of cores. */
                new AbstractMap.SimpleEntry<>("totalCoreNum", digit("long")),
                /* Optimizable amount of memory */
                new AbstractMap.SimpleEntry<>("cutMemNum", digit("long")),
                /* Total memory */
                new AbstractMap.SimpleEntry<>("totalMemNum", digit("long")),
                /* Create Time */
                new AbstractMap.SimpleEntry<>("createTime", date()),
                /* Update Time */
                new AbstractMap.SimpleEntry<>("updateTime", date())
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
