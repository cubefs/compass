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
public class ShuffleReadMetrics {

    @JsonProperty("Remote Blocks Fetched")
    private Long remoteBlocksFetched;

    @JsonProperty("Local Blocks Fetched")
    private Long localBlocksFetched;

    @JsonProperty("Remote Bytes Read")
    private Long remoteBytesRead;

    @JsonProperty("Remote Bytes Read To Disk")
    private Long remoteBytesReadToDisk;

    @JsonProperty("Local Bytes Read")
    private Long localBytesRead;

    @JsonProperty("Fetch Wait Time")
    private Long fetchWaitTime;

    /**
     * total record read
     */
    @JsonProperty("Total Records Read")
    private Long recordsRead;
}
