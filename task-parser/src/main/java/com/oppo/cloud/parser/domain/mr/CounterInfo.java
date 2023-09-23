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

package com.oppo.cloud.parser.domain.mr;

public class CounterInfo {

    public enum MRConfiguration {
        /**
         * map cpu vcores
         */
        MAP_CPU_VCORES("mapreduce.map.cpu.vcores"),
        MAP_MEMORY_MB("mapreduce.map.memory.mb"),
        REDUCE_CPU_VCORES("mapreduce.reduce.cpu.vcores"),
        REDUCE_MEMORY_MB("mapreduce.reduce.memory.mb"),
        YARN_INCREMENT_VCORES("yarn.scheduler.increment-allocation-vcores"),
        YARN_INCREMENT_MEMORY_MB("yarn.scheduler.increment-allocation-mb"),
        HIVE_QUERY_STRING("hive.query.string");

        private final String key;

        MRConfiguration(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

    }

    public enum CounterGroupName {
        /**
         * File System Counters
         */
        FILE_SYSTEM_COUNTER("org.apache.hadoop.mapreduce.FileSystemCounter"),
        JOB_COUNTER("org.apache.hadoop.mapreduce.JobCounter"),
        TASK_COUNTER("org.apache.hadoop.mapreduce.TaskCounter"),
        HIVE("HIVE"),
        SHUFFLE_ERRORS("Shuffle Errors"),
        FILE_INPUT_FORMAT_COUNTER("org.apache.hadoop.mapreduce.lib.input.FileInputFormatCounter"),
        FILE_OUTPUT_FORMAT_COUNTER("org.apache.hadoop.mapreduce.lib.output.FileOutputFormatCounter");

        private final String counterGroupName;

        CounterGroupName(String counterGroupName) {
            this.counterGroupName = counterGroupName;
        }

        public String getCounterGroupName() {
            return counterGroupName;
        }

    }

    public enum CounterName {
        /**
         * FileSystemCounter
         */
        FILE_LARGE_READ_OPS("FILE_LARGE_READ_OPS"),
        FILE_WRITE_OPS("FILE_WRITE_OPS"),
        HDFS_READ_OPS("HDFS_READ_OPS"),
        HDFS_BYTES_READ("HDFS_BYTES_READ"),
        HDFS_LARGE_READ_OPS("HDFS_LARGE_READ_OPS"),
        FILE_READ_OPS("FILE_READ_OPS"),
        FILE_BYTES_WRITTEN("FILE_BYTES_WRITTEN"),
        FILE_BYTES_READ("FILE_BYTES_READ"),
        HDFS_WRITE_OPS("HDFS_WRITE_OPS"),
        HDFS_BYTES_WRITTEN("HDFS_BYTES_WRITTEN"),

        /**
         * JobCounter
         */
        TOTAL_LAUNCHED_MAPS("TOTAL_LAUNCHED_MAPS"),
        VCORES_MILLIS_REDUCES("VCORES_MILLIS_REDUCES"),
        TOTAL_LAUNCHED_REDUCES("TOTAL_LAUNCHED_REDUCES"),
        NUM_KILLED_MAPS("NUM_KILLED_MAPS"),
        OTHER_LOCAL_MAPS("OTHER_LOCAL_MAPS"),
        NUM_KILLED_REDUCES("NUM_KILLED_REDUCES"),
        DATA_LOCAL_MAPS("DATA_LOCAL_MAPS"),
        MB_MILLIS_MAPS("MB_MILLIS_MAPS"),
        SLOTS_MILLIS_REDUCES("SLOTS_MILLIS_REDUCES"),
        VCORES_MILLIS_MAPS("VCORES_MILLIS_MAPS"),
        MB_MILLIS_REDUCES("MB_MILLIS_REDUCES"),
        SLOTS_MILLIS_MAPS("SLOTS_MILLIS_MAPS"),
        RACK_LOCAL_MAPS("RACK_LOCAL_MAPS"),
        MILLIS_REDUCES("MILLIS_REDUCES"),
        MILLIS_MAPS("MILLIS_MAPS"),

        /**
         * TaskCounter
         */
        MAP_OUTPUT_MATERIALIZED_BYTES("MAP_OUTPUT_MATERIALIZED_BYTES"),
        REDUCE_INPUT_RECORDS("REDUCE_INPUT_RECORDS"),
        SPILLED_RECORDS("SPILLED_RECORDS"),
        MERGED_MAP_OUTPUTS("MERGED_MAP_OUTPUTS"),
        VIRTUAL_MEMORY_BYTES("VIRTUAL_MEMORY_BYTES"),
        MAP_INPUT_RECORDS("MAP_INPUT_RECORDS"),
        SPLIT_RAW_BYTES("SPLIT_RAW_BYTES"),
        FAILED_SHUFFLE("FAILED_SHUFFLE"),
        MAP_OUTPUT_BYTES("MAP_OUTPUT_BYTES"),
        REDUCE_SHUFFLE_BYTES("REDUCE_SHUFFLE_BYTES"),
        PHYSICAL_MEMORY_BYTES("PHYSICAL_MEMORY_BYTES"),
        GC_TIME_MILLIS("GC_TIME_MILLIS"),
        REDUCE_INPUT_GROUPS("REDUCE_INPUT_GROUPS"),
        COMBINE_OUTPUT_RECORDS("COMBINE_OUTPUT_RECORDS"),
        SHUFFLED_MAPS("SHUFFLED_MAPS"),
        REDUCE_OUTPUT_RECORDS("REDUCE_OUTPUT_RECORDS"),
        MAP_OUTPUT_RECORDS("MAP_OUTPUT_RECORDS"),
        COMBINE_INPUT_RECORDS("COMBINE_INPUT_RECORDS"),
        CPU_MILLISECONDS("CPU_MILLISECONDS"),
        COMMITTED_HEAP_BYTES("COMMITTED_HEAP_BYTES"),


        /**
         * HIVE
         */
        CREATED_FILES("CREATED_FILES"),
        RECORDS_IN("RECORDS_IN"),
        RECORDS_OUT_0("RECORDS_OUT_0"),
        RECORDS_OUT_INTERMEDIATE("RECORDS_OUT_INTERMEDIATE"),

        /**
         * Shuffle Errors
         */
        CONNECTION("CONNECTION"),
        WRONG_LENGTH("WRONG_LENGTH"),
        BAD_ID("BAD_ID"),
        WRONG_MAP("WRONG_MAP"),
        WRONG_REDUCE("WRONG_REDUCE"),
        IO_ERROR("IO_ERROR"),

        /**
         * FileOutPutFormatCounter
         */
        BYTES_WRITTEN("BYTES_WRITTEN"),

        /**
         * FileInPutFormatCounter
         */
        BYTES_READ("BYTES_READ");

        private final String counterName;

        CounterName(String counterName) {
            this.counterName = counterName;
        }

        public String getCounterName() {
            return counterName;
        }

    }

}
