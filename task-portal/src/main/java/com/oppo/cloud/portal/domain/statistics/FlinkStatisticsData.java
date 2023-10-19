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

package com.oppo.cloud.portal.domain.statistics;

import lombok.Data;

/**
 * Overview of Flink Jobs Data Statistics
 */
@Data
public class FlinkStatisticsData {
    /**
     * Diagnostic jobs count
     */
    private long jobCount;
    /**
     * Exception jobs count
     */
    private long exceptionJobCount;
    /**
     * Ratio of exception jobs
     */
    private double exceptionJobRatio;
    /**
     * Optimizable resources job count
     */
    private long resourceJobCount;
    /**
     * Ratio of jobs with optimizable resources
     */
    private double resourceJobRatio;
    /**
     * Optimizable CPU count
     */
    private double decrCPUCount;
    /**
     * Total number of CPU
     */
    private double totalCPUCount;
    /**
     * Ratio of optimizable CPU
     */
    private double decrCPURatio;
    /**
     * Number of optimizable memory
     */
    private double decrMemory;
    /**
     * Total memory
     */
    private double totalMemory;
    /**
     * Ratio of optimizable memory
     */
    private double decrMemoryRatio;
}
