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

package com.oppo.cloud.flink.constant;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Getter
@Component
public class DiagnosisParamsConstants {
    /**
     * Default reduction rate
     */
    public Double parallelCutRate = 0.1d;
    /**
     * Default expansion(grow) rate
     */
    public Double tmParallelGrowRate = 0.1d;
    /**
     * Threshold value for low CPU utilization of a single TaskManager
     */
    public Double tmCpuUsageCutThreshold = 0.4d;
    /**
     * Target value for low CPU utilization of a single TaskManager
     */
    public Double tmCpuUsageCutTarget = 0.6d;
    /**
     * Threshold value for high CPU utilization
     */
    public Double tmCpuUsageGrowThreshold = 0.8d;
    /**
     * Maximum target value for peak CPU utilization of a single TaskManager
     */
    public Double tmCpuUsageGrowTarget = 0.6d;
    /**
     * Threshold value for low memory utilization of a single TaskManager
     */
    public Double tmMemUsageLowThreshold = 0.4d;
    /**
     * Target value for low memory utilization of a single TaskManager
     */
    public Double tmMemUsageLowTarget = 0.6d;
    /**
     * Threshold value for high memory utilization
     */
    public Double tmMemUsageHighThreshold = 0.8d;
    /**
     * Target value for memory utilization of a single TaskManager
     */
    public Double tmMemUsageHighTarget = 0.6d;
    /**
     * Threshold value for the duration (in seconds) of high peak utilization of a single TaskManager
     */
    public Float tmPeakHighTimeThreshold = 1800f;
    /**
     * Duration of high cumulative CPU utilization
     */
    public Float cpuUsageAccHighTimeRate = 0.2f;
    /**
     * Maximum time after which diagnosis starts within a cycle
     */
    public Integer diagnosisAfterMinutesMax = 60;
    /**
     * Minimum value of LAG used in diagnostic judgments to determine the end of data chasing
     */
    public Integer diagnosisMinDelayAfterRunning = 100;
    /**
     * During scaling out, take the average CPU utilization of the last n minutes.
     */
    public Integer tmCpuHighLatestNMinutes = 5;
    /**
     * When chasing latency, judge whether the latency time exceeds this threshold value.
     * If it exceeds, it is necessary to chase the latency.
     */
    public Float catchUpDelayThreshold = 300f;
    /**
     * Threshold value of maximum ratio of consumption rate to production rate when chasing latency
     */
    public Float catchUpConsumeDivideProduceThreshold = 3.0f;
    /**
     * Minimum interval for scaling tasks (in minutes)
     */
    public Float elasticMinInterval = 5.0f;
    /**
     * When there are 100 TaskManagers in one JobManager, the memory of JobManager is set to 1 GB.
     */
    public Integer jm1gTmNum = 100;
    /**
     * Threshold value of usage difference for Input/Output buffer pool of slow operator(Vertices)
     */
    public Double slowVerticesInoutDiffHighThreshold = 0.6;
    /**
     * Threshold value of duration (in seconds) for usage difference of Input/Output buffer pool of slow operator(Vertices)
     */
    public Integer slowVerticesInoutDiffHighDuration = 300;
    /**
     * Expected usage rate of Manage memory
     */
    public Float tmManageMemUsageCutThreshold = 0.5f;
    /**
     * Proportion of distance between the center of traffic trough and the edge
     */
    public Float trafficTroughRatio = 0.7f;

    /**
     * Maximum memory value of TM
     */
    public Integer tmMemMax = 6144;
    /**
     * Minimum memory value of TM
     */
    public Integer tmMemMin = 1024;
    /**
     * Threshold value for unused CPU of TM
     */
    public Double tmCpuNoUsageThreshold = 0.05;
    /**
     * Threshold value for unused memory of TM
     */
    public Double tmHeapMemNoUsageThreshold = 0.1;
    /**
     * Threshold value of delay time for task reduction (in seconds)
     */
    public Float JOB_CUT_LAG_TIME_THRESHOLD = 600f;
    /**
     * Threshold value of delay time for task reduction (in seconds)
     */
    public Float JOB_DELAY_LITTLE_HIGH = 300f;
    /**
     * Default maximum parallelism of the job
     */
    public Integer maxParallel = 300;
    /**
     * Start time of hourly diagnosis
     */
    public Integer hourlyDiagnosisStartMinutes = 60;
    /**
     * Diagnosis at the hourly level is performed within start minutes and end minutes after the task is online.
     */
    public Integer hourlyDiagnosisEndMinutes = 120;
}
