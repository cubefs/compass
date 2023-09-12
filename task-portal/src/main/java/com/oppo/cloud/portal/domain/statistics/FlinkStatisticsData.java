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
 * Flink作业概览数据统计
 */
@Data
public class FlinkStatisticsData {
    /*诊断作业数*/
    private long jobCount;
    /*异常作业数*/
    private long exceptionJobCount;
    /*异常作业占比*/
    private double exceptionJobRatio;
    /*可优化资源作业数*/
    private long resourceJobCount;
    /*可优化资源作业占比*/
    private double resourceJobRatio;
    /*可优化CPU数*/
    private double decrCPUCount;
    /*总CPU数*/
    private double totalCPUCount;
    /*可优化CPU占比*/
    private double decrCPURatio;
    /*可优化内存数*/
    private double decrMemory;
    /*总内存*/
    private double totalMemory;
    /*可优化内存占比*/
    private double decrMemoryRatio;
}
