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

package com.oppo.cloud.common.domain.eventlog.config;

import lombok.Data;

/**
 * Hdfs stuck configuration
 */
@Data
public class HdfsStuckConfig {

    private Boolean disable;

    /**
     * Proportion of max/median
     */
    private Float threshold;

    /**
     * task reads data volume in MB
     */
    private Float taskSize;

    /**
     * Duration (in seconds)
     */
    private Long duration;

}
