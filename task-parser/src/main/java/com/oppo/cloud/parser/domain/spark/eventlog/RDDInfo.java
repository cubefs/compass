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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.List;

@Data
public class RDDInfo {

    @JsonProperty("RDD ID")
    private Integer id;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Number of Partitions")
    private Integer numPartitions;

    @JsonProperty("Storage Level")
    private StorageLevel storageLevel;

    @JsonProperty("Parent IDs")
    private List<Integer> parentIds;

    @JsonProperty("Callsite")
    private String callSite;

    @JsonDeserialize(converter = RDDScopeConverter.class)
    @JsonProperty("Scope")
    private RDDOperationScope scope;

    @JsonProperty("Number of Cached Partitions")
    private Integer numCachedPartitions;

    @JsonProperty("Memory Size")
    private Long memSize;

    @JsonProperty("Disk Size")
    private Long diskSize;
}
