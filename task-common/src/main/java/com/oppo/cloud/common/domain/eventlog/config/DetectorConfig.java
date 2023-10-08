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

import com.oppo.cloud.common.domain.mr.config.*;
import lombok.Data;

/**
 * Threshold values for anomaly detection
 */
@Data
public class DetectorConfig {

    /**
     * spark environment
     */
    private SparkEnvironmentConfig sparkEnvironmentConfig;

    /**
     * Speculative execution of task configuration
     */
    private SpeculativeTaskConfig speculativeTaskConfig;

    /**
     * Memory waste configuration
     */
    private MemWasteConfig memWasteConfig;

    /**
     * CPU waste configuration
     */
    private CpuWasteConfig cpuWasteConfig;

    /**
     * Job duration anomaly configuration
     */
    private JobDurationConfig jobDurationConfig;

    /**
     * Stage duration anomaly configuration
     */
    private StageDurationConfig stageDurationConfig;

    /**
     * Task long-tail configuration
     */
    private TaskDurationConfig taskDurationConfig;

    /**
     * HDFS stuck configuration
     */
    private HdfsStuckConfig hdfsStuckConfig;

    /**
     * Global sorting configuration
     */
    private GlobalSortConfig globalSortConfig;

    /**
     * Large table scan configuration
     */
    private LargeTableScanConfig largeTableScanConfig;

    /**
     * OOM warning configuration
     */
    private OOMWarnConfig oomWarnConfig;

    /**
     * Data skew configuration
     */
    private DataSkewConfig dataSkewConfig;
    /**
     * MapReduce env configuration
     */

    private MREnvironmentConfig mrEnvironmentConfig;

    /**
     * MapReduce memory configuration
     */
    private MRMemWasteConfig mrMemWasteConfig;

    /**
     * MapReduce large table configuration
     */
    private MRLargeTableScanConfig mrLargeTableScanConfig;

    /**
     * MapReduce data skew configuration
     */
    private MRDataSkewConfig mrDataSkewConfig;

    /**
     * MapReduce speculative task configuration
     */
    private MRSpeculativeTaskConfig mrSpeculativeTaskConfig;

    /**
     * MapReduce task duration configuration
     */
    private MRTaskDurationConfig mrTaskDurationConfig;

    /**
     * MapReduce gc configuration
     */
    private MRGCConfig mrGCConfig;
}
