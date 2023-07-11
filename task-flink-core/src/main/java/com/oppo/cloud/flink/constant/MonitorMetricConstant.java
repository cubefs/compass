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
 * 使用Prometheus监控查询指标数据类
 */
public class MonitorMetricConstant {

    /**
     * query api
     */
    public static final String QUERY_RANGE_QUERY = "/api/v1/query_range?query=";
    /**
     * tm,整体平均cpu使用率,0-1
     */
    public static final String TM_AVG_CPU_USAGE_RATE = "avg(rate(flink_taskmanager_Status_JVM_CPU_Time"
            + "{}[2m])/ ( 1000 * 1000 * 1000 ))";
    /**
     * 单个tm进程的cpu使用率,0-1
     */
//    public static final String TM_CPU_USAGE_RATE = "avg(flink_taskmanager_Status_JVM_CPU_Load{}) by (tm_id)";
    public static final String TM_CPU_USAGE_RATE = "avg(rate(flink_taskmanager_Status_JVM_CPU_Time{}[2m]) / 1000000000) by (tm_id)";
    /**
     * 单个tm堆内存使用率
     */
    public static final String TM_HEAP_MEM_USAGE_RATE = "max(flink_taskmanager_Status_JVM_Memory_Heap_Used"
            + "{}) by (tm_id) /"
            + " max(flink_taskmanager_Status_JVM_Memory_Heap_Max{"
            + "}) by (tm_id)";
    /**
     * task 总slot数量
     */
    public static final String TOTAL_TM_SLOT_COUNTS = "flink_jobmanager_taskSlotsTotal{}";
    /**
     * tm数量
     */
    public static final String TOTAL_TM_COUNTS = "max(flink_jobmanager_numRegisteredTaskManagers{})";

    /**
     * 作业running时长,单位毫秒,伸缩后这个时间会从0开始
     */
    public static final String JOB_UP_TIME = "flink_jobmanager_job_uptime{}";

    /**
     * 慢算子阻塞度明细
     */
    public static final String SLOW_VERTICES = "max(flink_taskmanager_job_task_buffers_inPoolUsage{}) by(tm_id,task_name) - max(flink_taskmanager_job_task_buffers_outPoolUsage{}) by(tm_id,task_name)";
    /**
     * 反压算子
     */
    public static final String BACK_PRESSURE_VERTICES = "max(flink_taskmanager_job_task_isBackPressured{}) by (tm_id,task_name)";
    /**
     * TM manage 内存使用量
     */
    public static final String TM_MANAGE_MEM_USAGE = "max(flink_taskmanager_Status_Flink_Memory_Managed_Used{}) by (tm_id)";
    /**
     * TM manage 内存总量
     */
    public static final String TM_MANAGE_MEM_TOTAL = "max(flink_taskmanager_Status_Flink_Memory_Managed_Total{}) by (tm_id)";
    /**
     * tm总的堆内存
     */
    public static final String TM_TOTAL_HEAP_MEM_MAX = "max(flink_taskmanager_Status_JVM_Memory_Heap_Max{}) by (tm_id)";
    /**
     * tm使用堆内存
     */
    public static final String TM_USAGE_HEAP_MEM_MAX = "max(flink_taskmanager_Status_JVM_Memory_Heap_Used{}) by (tm_id)";


    /**
     * 不同版本的flink kafka SCOPE不同
     */
    public static final String KAFKA_SCOPE = "KafkaConsumer";
    /**
     * tm的流量
     */
    public static final String TM_DATA_FLOW_RATE = String.format("sum(flink_taskmanager_job_task_operator_%s_bytes_consumed_rate{}) by (tm_id)", KAFKA_SCOPE);

    /**
     * 延迟记录数
     */
    public static final String SUM_RECORDS_LAG_PROMQL = String.format("sum (flink_taskmanager_job_task_operator_%s_records_lag_max{})", KAFKA_SCOPE);
    /**
     * 延迟时间秒
     */
    public static final String MAX_TIME_LAG_PROMQL = String.format("sum(flink_taskmanager_job_task_operator_%s_records_lag_max", KAFKA_SCOPE)
            + "{}  )/"
            + String.format("sum(flink_taskmanager_job_task_operator_%s_records_consumed_rate{", KAFKA_SCOPE)
            + "}  )";
    /**
     * kafka offset的偏移量
     */
    public static final String OFFSET_DELTA = String.format("sum(delta(flink_taskmanager_job_task_operator_%s_records_lag_max", KAFKA_SCOPE)
            + "{}[2m])) ";
    /**
     * 消费速率除以生产速率
     */
    public static final String CONSUME_DIVIDE_PRODUCE_RATE = "1 / ((sum(delta("
            + String.format("flink_taskmanager_job_task_operator_%s_records_lag_max{", KAFKA_SCOPE)
            + String.format("}[2m])) / 120) / clamp_min (sum(flink_taskmanager_job_task_operator_%s_records_consumed_rate{", KAFKA_SCOPE)
            + "}),1) + 1)";
    /**
     * 流量,作业每秒从kafka读取的数据量,单位bytes
     */
    public static final String JOB_DATA_FLOW_RATE = String.format("sum(flink_taskmanager_job_task_operator_%s_bytes_consumed_rate{", KAFKA_SCOPE)
            + "})";
    /**
     * 流量，用于计算流量波谷，按小时的step来获取metrics
     */
    public static final String JOB_DATA_FLOW_RATE_TROUGH = String.format("trough_sum(flink_taskmanager_job_task_operator_%s_bytes_consumed_rate{})", KAFKA_SCOPE);
    public static final String KAFKA_PARTITIONS = String.format("sum(flink_taskmanager_job_task_operator_%s_assigned_partitions{})", KAFKA_SCOPE);

}
