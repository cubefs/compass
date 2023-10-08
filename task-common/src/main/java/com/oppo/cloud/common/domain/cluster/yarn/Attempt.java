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

package com.oppo.cloud.common.domain.cluster.yarn;

import lombok.Data;

/**
 * sparK rest api app attempt
 */
@Data
public class Attempt {

    /**
     * Attempt id
     */
    private String attemptId;
    /**
     * Start time
     */
    private String startTime;
    /**
     * End time
     */
    private String endTime;
    /**
     * Last update time
     */
    private String lastUpdated;
    /**
     * Running time
     */
    private Integer duration;
    /**
     * Spark running user
     */
    private String sparkUser;
    /**
     * Whether it is completed or not
     */
    private Boolean completed;
    /**
     * version
     */
    private String appSparkVersion;
    /**
     * Start time
     */
    private Long startTimeEpoch;
    /**
     * End time
     */
    private Long endTimeEpoch;
    /**
     * Last updated time(epoch)
     */
    private Long lastUpdatedEpoch;
}
