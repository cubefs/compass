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

package com.oppo.cloud.flink.domain.diagnosis;


import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Diagnosis entity.
 */
@Data
@Accessors(chain = true)
public class RcJobDiagnosis {

    private static final long serialVersionUID = 1L;
    /**
     * Job name reported by Prometheus.
     */
    private String jobName;
    /**
     * Parallelism
     */
    private Integer parallel;
    /**
     * Number of slots of a TM.
     */
    private Integer tmSlotNum;
    /**
     * Number of TaskManagers (TMs).
     */
    private Integer tmNum;
    /**
     * tm memory
     */
    private Integer tmMem;
    /**
     * jm memory
     */
    private Integer jmMem;
    /**
     * tm core
     */
    private Integer tmCore;
    /**
     * kafka partition number
     */
    private Integer kafkaConsumePartitionNum;
    /**
     * Suggested parallelism.
     */
    private Integer diagnosisParallel;
    /**
     * Suggested number of slots for a TaskManager.
     */
    private Integer diagnosisTmSlot;
    /**
     * Recommended number of TaskManagers (TMs).
     */
    private Integer diagnosisTmNum;
    /**
     * Suggested number of slots for a TaskManager.
     */
    private Integer diagnosisTmCore;
    /**
     * Suggested memory(in MB) for a TaskManager
     */
    private Integer diagnosisTmMem;
    /**
     * Suggested memory (in MB) for JobManager
     */
    private Integer diagnosisJmMem;
    /**
     * Maximum value of overall average CPU utilization of a TM.
     */
    private Float tmAvgCpuUsageMax;
    /**
     * Minimum value of overall average CPU utilization of a TM.
     */
    private Float tmAvgCpuUsageMin;
    /**
     * Average value of overall average CPU utilization of a TM.
     */
    private Float tmAvgCpuUsageAvg;
}
