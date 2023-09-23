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

package com.oppo.cloud.parser.domain.spark.eventlog;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class StageInfo {

    @JsonProperty("Stage ID")
    private Integer stageId;

    @JsonProperty("Stage Attempt ID")
    private Integer attemptNumber;

    @JsonProperty("Stage Name")
    private String name;

    @JsonProperty("Number of Tasks")
    private Integer numTasks;

    @JsonProperty("RDD Info")
    private List<RDDInfo> rddInfos;

    @JsonProperty("Parent IDs")
    private List<Integer> parentIds;

    @JsonProperty("Details")
    private String details;

    @JsonProperty("Task Metrics")
    private TaskMetrics taskMetrics;

    @JsonProperty("Submission Time")
    private Long submissionTime;

    @JsonProperty("Completion Time")
    private Long completeTime;

    @JsonProperty("Failure Reason")
    private String failureReason;

    @JsonProperty("Accumulables")
    private List<AccumulableInfo> accumulables;
}
