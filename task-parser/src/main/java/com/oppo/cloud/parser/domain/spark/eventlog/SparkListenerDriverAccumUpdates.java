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

/**
 * * @param executionId The execution id for a query, so we can find the query plan.
 *  * @param accumUpdates Map from accumulator id to the metric value (metrics are always 64-bit ints).
 */
@Data
public class SparkListenerDriverAccumUpdates extends SparkListenerEvent {

    @JsonProperty("executionId")
    private Long executionId;

    @JsonProperty("accumUpdates")
    private List<List<Long>> accumUpdates;
}
