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

@Data
public class StorageLevel {

    @JsonProperty("Use Disk")
    private boolean useDisk;

    @JsonProperty("Use Memory")
    private boolean useMemory;

    /**
     * 2.4.0 没有
     */
    @JsonProperty("Use OffHeap")
    private boolean useOffHeap;

    @JsonProperty("Deserialized")
    private boolean deserialized;

    @JsonProperty("Replication")
    private Integer replication;
}
