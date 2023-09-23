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
public class TaskEndReason {

    /**
     * Success
     */
    @JsonProperty("Reason")
    private String Reason;

    /**
     * TaskFailedReason
     * FetchFailed
     */
    @JsonProperty("Block Manager Address")
    private BlockManagerId blockManagerAddress;

    @JsonProperty("Shuffle ID")
    private Integer shuffleId;

    @JsonProperty("Map ID")
    private Integer mapId;

    @JsonProperty("Reduce ID")
    private Integer reduceId;

    @JsonProperty("Message")
    private String message;

    /**
     * ExceptionFailure
     */
    @JsonProperty("Class Name")
    private String className;

    @JsonProperty("Description")
    private String description;

    @JsonProperty("Stack Trace")
    private List<StackTraceElement> stackTrace;

    @JsonProperty("Full Stack Trace")
    private String fullStackTrace;

    @JsonProperty("Accumulator Updates")
    private List<AccumulableInfo> accumUpdates;
    /**
     * TaskCommitDenied
     */
    @JsonProperty("Job ID")
    private Integer jobID;

    @JsonProperty("Partition ID")
    private Integer partitionID;

    @JsonProperty("Attempt Number")
    private Integer attemptNumber;

    /**
     * ExecutorLostFailure
     */
    @JsonProperty("Executor ID")
    private String execId;

    @JsonProperty("Exit Caused By App")
    private boolean exitCausedByApp;

    @JsonProperty("Loss Reason")
    private String lossReason;

    /**
     * TaskKilled
     */
    @JsonProperty("Kill Reason")
    private String killReason;

}
