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

import com.oppo.cloud.common.domain.mr.config.MRDataSkewConfig;
import com.oppo.cloud.common.domain.mr.config.MREnvironmentConfig;
import com.oppo.cloud.common.domain.mr.config.MRLargeTableScanConfig;
import com.oppo.cloud.common.domain.mr.config.MRMemWasteConfig;
import lombok.Data;

/**
 * 异常检测各项阈值
 */
@Data
public class DetectorConfig {

    /**
     * spark environment
     */
    private SparkEnvironmentConfig sparkEnvironmentConfig;

    /**
     * 推测执行Task配置
     */
    private SpeculativeTaskConfig speculativeTaskConfig;

    /**
     * 内存浪费配置
     */
    private MemWasteConfig memWasteConfig;

    /**
     * CPU浪费配置
     */
    private CpuWasteConfig cpuWasteConfig;

    /**
     * Job耗时异常配置
     */
    private JobDurationConfig jobDurationConfig;

    /**
     * Stage耗时异常配置
     */
    private StageDurationConfig stageDurationConfig;

    /**
     * Task长尾配置
     */
    private TaskDurationConfig taskDurationConfig;

    /**
     * hdfs卡顿配置
     */
    private HdfsStuckConfig hdfsStuckConfig;

    /**
     * 全局排序配置
     */
    private GlobalSortConfig globalSortConfig;

    /**
     * 大表扫描配置
     */
    private LargeTableScanConfig largeTableScanConfig;

    /**
     * OOM预警
     */
    private OOMWarnConfig oomWarnConfig;

    /**
     * 数据倾斜配置
     */
    private DataSkewConfig dataSkewConfig;
    /**
     * MapReduce env config
     */

    private MREnvironmentConfig mrEnvironmentConfig;

    /**
     * MapReduce memory config
     */
    private MRMemWasteConfig mrMemWasteConfig;

    /**
     * MapReduce large table config
     */
    private MRLargeTableScanConfig mrLargeTableScanConfig;

    /**
     * MapReduce data skew config
     */
    private MRDataSkewConfig mrDataSkewConfig;
}
