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
public class TaskMetrics {

    @JsonProperty("Executor Deserialize Time")
    private Long executorDeserializeTime;

    @JsonProperty("Executor Deserialize CPU Time")
    private Long executorDeserializeCpuTime;

    @JsonProperty("Executor Run Time")
    private Long executorRunTime;

    @JsonProperty("Executor CPU Time")
    private Long executorCpuTime;

    @JsonProperty("Result Size")
    private Long resultSize;

    @JsonProperty("JVM GC Time")
    private Long jvmGCTime;

    @JsonProperty("Result Serialization Time")
    private Long resultSerializationTime;

    @JsonProperty("Memory Bytes Spilled")
    private Long memoryBytesSpilled;

    @JsonProperty("Disk Bytes Spilled")
    private Long diskBytesSpilled;
    /**
     * 2.4.0没有
     */
    @JsonProperty("peakExecutionMemory")
    private Long peakExecutionMemory;

    @JsonProperty("Shuffle Read Metrics")
    private ShuffleReadMetrics shuffleReadMetrics;

    @JsonProperty("Shuffle Write Metrics")
    private ShuffleWriteMetrics shuffleWriteMetrics;

    @JsonProperty("Input Metrics")
    private InputMetrics inputMetrics;

    @JsonProperty("Output Metrics")
    private OutputMetrics outputMetrics;

    @JsonProperty("Updated Blocks")
    private List<UpdateBlockStatus> updatedBlockStatues;
}
