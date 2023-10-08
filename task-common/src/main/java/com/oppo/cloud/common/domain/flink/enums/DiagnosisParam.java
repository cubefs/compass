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

package com.oppo.cloud.common.domain.flink.enums;

/**
 * Diagnosis Parameters
 */
public enum DiagnosisParam {
    /**
     * Total partitions of source
     */
    PartitionSum(0, "source分区总数"),
    /**
     * When the CPU is low, it needs to be increased to the target value of the CPU.
     */
    CpuLowTarget(1, "CPU较低时，需要增大到cpu的目标值"),
    /**
     * The threshold for low CPU.
     */
    CpuLowThreshold(2, "CPU低的阈值"),
    /**
     * When the CPU is high, it needs to be decreased to the target value of the CPU.
     */
    CpuHighTarget(3, "CPU较高时，需要减小到cpu的目标值"),
    /**
     * The threshold for high CPU.
     */
    CpuHighThreshold(4, "CPU高的阈值"),
    /**
     * CPU growth change rate.
     */
    GrowCpuChangeRate(5, "CPU 增长change rate"),
    /**
     * Maximum traffic.
     */
    FlowMax(6, "最大流量"),
    /**
     * When the memory (Mem) is low, it needs to be increased to the target value of the memory.
     */
    MemoryLowTarget(7, "Mem较低时，需要增大到Mem的目标值"),
    /**
     * The threshold for low memory.
     */
    MemoryLowThreshold(8, "Mem低的阈值"),
    /**
     * When the memory (Mem) is high, it needs to be decreased to the target value of the memory.
     */
    MemoryHighTarget(9, "Mem较高时，需要减小到Mem的目标值"),
    /**
     * The threshold for high memory.
     */
    MemoryHighThreshold(10, "Mem高的阈值"),
    /**
     * Flink jobmanager Id
     */
    JobId(11, "flink 的jm 的id"),
    /**
     * Flink taskmanager Id
     */
    TmIds(12, "flink 的tm 的id"),
    /**
     * Flink Job reported from prometheus.
     */
    Job(13, "flink 上报prometheus的job"),
    ;

    private final int code;
    private final String desc;

    DiagnosisParam(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
