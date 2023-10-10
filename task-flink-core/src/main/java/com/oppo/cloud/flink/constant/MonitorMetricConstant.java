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

/**
 * Querying metrics data using Prometheus monitoring
 */
public class MonitorMetricConstant {

    /**
     * query api
     */
    public static final String QUERY_RANGE_QUERY = "/api/v1/query_range?query=";
    /**
     * TM overall average CPU utilization, ranging from 0 to 1
     */
    public static final String TM_AVG_CPU_USAGE_RATE = "avg(rate(flink_taskmanager_Status_JVM_CPU_Time"
            + "{}[2m]) / ( 1000 * 1000 * 1000 ))";
    /**
     * CPU utilization of a single TM process, ranging from 0 to 1
     */
//    public static final String TM_CPU_USAGE_RATE = "avg(flink_taskmanager_Status_JVM_CPU_Load{}) by (tm_id)";
    public static final String TM_CPU_USAGE_RATE = "avg(rate(flink_taskmanager_Status_JVM_CPU_Time{}[2m]) / 1000000000) by (tm_id)";
    /**
     * Memory utilization of a single TM heap.
     */
    public static final String TM_HEAP_MEM_USAGE_RATE = "max(flink_taskmanager_Status_JVM_Memory_Heap_Used"
            + "{}) by (tm_id) /"
            + " max(flink_taskmanager_Status_JVM_Memory_Heap_Max{"
            + "}) by (tm_id)";
    /**
     * Total number of slots in the task.
     */
    public static final String TOTAL_TM_SLOT_COUNTS = "flink_jobmanager_taskSlotsTotal{}";
    /**
     * Number of TaskManagers.
     */
    public static final String TOTAL_TM_COUNTS = "max(flink_jobmanager_numRegisteredTaskManagers{})";
    /**
     * Duration of job running, in milliseconds. This time starts from 0 after scaling.
     */
    public static final String JOB_UP_TIME = "flink_jobmanager_job_uptime{}";

    /**
     * Details of the blocking degree of slow operators(vertices).
     */
    public static final String SLOW_VERTICES = "max(flink_taskmanager_job_task_buffers_inPoolUsage{}) by(tm_id,task_name) - max(flink_taskmanager_job_task_buffers_outPoolUsage{}) by(tm_id,task_name)";
    /**
     * Backpressure operators(vertices).
     */
    public static final String BACK_PRESSURE_VERTICES = "max(flink_taskmanager_job_task_isBackPressured{}) by (tm_id,task_name)";
    /**
     * Memory usage of TM Manage.
     */
    public static final String TM_MANAGE_MEM_USAGE = "max(flink_taskmanager_Status_Flink_Memory_Managed_Used{}) by (tm_id)";
    /**
     * Total memory of TM Manage.
     */
    public static final String TM_MANAGE_MEM_TOTAL = "max(flink_taskmanager_Status_Flink_Memory_Managed_Total{}) by (tm_id)";
    /**
     * Total heap memory of the TM.
     */
    public static final String TM_TOTAL_HEAP_MEM_MAX = "max(flink_taskmanager_Status_JVM_Memory_Heap_Max{}) by (tm_id)";
    /**
     * Used heap memory of the TM.
     */
    public static final String TM_USAGE_HEAP_MEM_MAX = "max(flink_taskmanager_Status_JVM_Memory_Heap_Used{}) by (tm_id)";
    /**
     * Different versions of Flink and Kafka have different SCOPE.
     */
    public static final String KAFKA_SCOPE = "KafkaConsumer";
    /**
     * Traffic of the TM.
     */
    public static final String TM_DATA_FLOW_RATE = String.format("sum(flink_taskmanager_job_task_operator_%s_bytes_consumed_rate{}) by (tm_id)", KAFKA_SCOPE);
    /**
     * Number of latency records.
     */
    public static final String SUM_RECORDS_LAG_PROMQL = String.format("sum (flink_taskmanager_job_task_operator_%s_records_lag_max{})", KAFKA_SCOPE);
    /**
     * Latency time in seconds.
     */
    public static final String MAX_TIME_LAG_PROMQL = String.format("sum(flink_taskmanager_job_task_operator_%s_records_lag_max", KAFKA_SCOPE)
            + "{}  )/"
            + String.format("sum(flink_taskmanager_job_task_operator_%s_records_consumed_rate{", KAFKA_SCOPE)
            + "}  )";
    /**
     * Offset of a Kafka message.
     */
    public static final String OFFSET_DELTA = String.format("sum(delta(flink_taskmanager_job_task_operator_%s_records_lag_max", KAFKA_SCOPE)
            + "{}[2m])) ";
    /**
     * Consumption rate divided by production rate.
     */
    public static final String CONSUME_DIVIDE_PRODUCE_RATE = "1 / ((sum(delta("
            + String.format("flink_taskmanager_job_task_operator_%s_records_lag_max{", KAFKA_SCOPE)
            + String.format("}[2m])) / 120) / clamp_min (sum(flink_taskmanager_job_task_operator_%s_records_consumed_rate{", KAFKA_SCOPE)
            + "}),1) + 1)";
    /**
     * Traffic, the amount of data read from Kafka by the job per second, in bytes.
     */
    public static final String JOB_DATA_FLOW_RATE = String.format("sum(flink_taskmanager_job_task_operator_%s_bytes_consumed_rate{", KAFKA_SCOPE)
            + "})";
    /**
     * Traffic, used to calculate traffic trough, obtain metrics with hourly step.
     */
    public static final String JOB_DATA_FLOW_RATE_TROUGH = String.format("trough_sum(flink_taskmanager_job_task_operator_%s_bytes_consumed_rate{})", KAFKA_SCOPE);
    public static final String KAFKA_PARTITIONS = String.format("sum(flink_taskmanager_job_task_operator_%s_assigned_partitions{})", KAFKA_SCOPE);

}
