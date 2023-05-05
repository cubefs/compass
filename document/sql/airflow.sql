/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

# Example of version 2.5.1, you need to be consistent with the version you actually use.

CREATE TABLE `tb_task_instance`
(
  `task_id` varchar(250) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `dag_id` varchar(250) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `run_id` varchar(250) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `map_index` int(11) NOT NULL DEFAULT '-1',
  `start_date` timestamp(6) NULL DEFAULT NULL,
  `end_date` timestamp(6) NULL DEFAULT NULL,
  `duration` float DEFAULT NULL,
  `state` varchar(20) DEFAULT NULL,
  `try_number` int(11) DEFAULT NULL,
  `max_tries` int(11) DEFAULT '-1',
  `hostname` varchar(1000) DEFAULT NULL,
  `unixname` varchar(1000) DEFAULT NULL,
  `job_id` int(11) DEFAULT NULL,
  `pool` varchar(256) NOT NULL,
  `pool_slots` int(11) NOT NULL,
  `queue` varchar(256) DEFAULT NULL,
  `priority_weight` int(11) DEFAULT NULL,
  `operator` varchar(1000) DEFAULT NULL,
  `queued_dttm` timestamp(6) NULL DEFAULT NULL,
  `queued_by_job_id` int(11) DEFAULT NULL,
  `pid` int(11) DEFAULT NULL,
  `executor_config` blob,
  `updated_at` timestamp(6) NULL DEFAULT NULL,
  `external_executor_id` varchar(250) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `trigger_id` int(11) DEFAULT NULL,
  `trigger_timeout` datetime DEFAULT NULL,
  `next_method` varchar(1000) DEFAULT NULL,
  `next_kwargs` json DEFAULT NULL,
  PRIMARY KEY (`dag_id`,`task_id`,`run_id`,`map_index`),
  KEY `ti_job_id` (`job_id`),
  KEY `ti_state_lkp` (`dag_id`,`task_id`,`run_id`,`state`),
  KEY `ti_trigger_id` (`trigger_id`),
  KEY `ti_pool` (`pool`,`state`,`priority_weight`),
  KEY `ti_dag_run` (`dag_id`,`run_id`),
  KEY `ti_dag_state` (`dag_id`,`state`),
  KEY `ti_state` (`state`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `tb_dag_run`
(
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `dag_id` varchar(250) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `queued_at` timestamp(6) NULL DEFAULT NULL,
  `execution_date` timestamp(6) NOT NULL,
  `start_date` timestamp(6) NULL DEFAULT NULL,
  `end_date` timestamp(6) NULL DEFAULT NULL,
  `state` varchar(50) DEFAULT NULL,
  `run_id` varchar(250) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `creating_job_id` int(11) DEFAULT NULL,
  `external_trigger` tinyint(1) DEFAULT NULL,
  `run_type` varchar(50) NOT NULL,
  `conf` blob,
  `data_interval_start` timestamp(6) NULL DEFAULT NULL,
  `data_interval_end` timestamp(6) NULL DEFAULT NULL,
  `last_scheduling_decision` timestamp(6) NULL DEFAULT NULL,
  `dag_hash` varchar(32) DEFAULT NULL,
  `log_template_id` int(11) DEFAULT NULL,
  `updated_at` timestamp(6) NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `dag_run_dag_id_execution_date_key` (`dag_id`,`execution_date`),
  UNIQUE KEY `dag_run_dag_id_run_id_key` (`dag_id`,`run_id`),
  KEY `task_instance_log_template_id_fkey` (`log_template_id`),
  KEY `dag_id_state` (`dag_id`,`state`),
  KEY `idx_dag_run_queued_dags` (`state`,`dag_id`),
  KEY `idx_last_scheduling_decision` (`last_scheduling_decision`),
  KEY `idx_dag_run_running_dags` (`state`,`dag_id`),
  KEY `idx_dag_run_dag_id` (`dag_id`)
) ENGINE=InnoDB AUTO_INCREMENT=63 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `tb_ab_user`
(
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `first_name` varchar(64) NOT NULL,
  `last_name` varchar(64) NOT NULL,
  `username` varchar(256) NOT NULL,
  `password` varchar(256) DEFAULT NULL,
  `active` tinyint(1) DEFAULT NULL,
  `email` varchar(256) NOT NULL,
  `last_login` datetime DEFAULT NULL,
  `login_count` int(11) DEFAULT NULL,
  `fail_login_count` int(11) DEFAULT NULL,
  `created_on` datetime DEFAULT NULL,
  `changed_on` datetime DEFAULT NULL,
  `created_by_fk` int(11) DEFAULT NULL,
  `changed_by_fk` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ab_user_username_uq` (`username`),
  UNIQUE KEY `ab_user_email_uq` (`email`),
  KEY `ab_user_created_by_fk_fkey` (`created_by_fk`),
  KEY `ab_user_changed_by_fk_fkey` (`changed_by_fk`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `tb_dag`
(
  `dag_id` varchar(250) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `root_dag_id` varchar(250) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `is_paused` tinyint(1) DEFAULT NULL,
  `is_subdag` tinyint(1) DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT NULL,
  `last_parsed_time` timestamp(6) NULL DEFAULT NULL,
  `last_pickled` timestamp(6) NULL DEFAULT NULL,
  `last_expired` timestamp(6) NULL DEFAULT NULL,
  `scheduler_lock` tinyint(1) DEFAULT NULL,
  `pickle_id` int(11) DEFAULT NULL,
  `fileloc` varchar(2000) DEFAULT NULL,
  `processor_subdir` varchar(2000) DEFAULT NULL,
  `owners` varchar(2000) DEFAULT NULL,
  `description` text,
  `default_view` varchar(25) DEFAULT NULL,
  `schedule_interval` text,
  `timetable_description` varchar(1000) DEFAULT NULL,
  `max_active_tasks` int(11) NOT NULL,
  `max_active_runs` int(11) DEFAULT NULL,
  `has_task_concurrency_limits` tinyint(1) NOT NULL,
  `has_import_errors` tinyint(1) DEFAULT '0',
  `next_dagrun` timestamp(6) NULL DEFAULT NULL,
  `next_dagrun_data_interval_start` timestamp(6) NULL DEFAULT NULL,
  `next_dagrun_data_interval_end` timestamp(6) NULL DEFAULT NULL,
  `next_dagrun_create_after` timestamp(6) NULL DEFAULT NULL,
  PRIMARY KEY (`dag_id`),
  KEY `idx_root_dag_id` (`root_dag_id`),
  KEY `idx_next_dagrun_create_after` (`next_dagrun_create_after`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tb_serialized_dag` (
  `dag_id` varchar(250) NOT NULL,
  `fileloc` varchar(2000) NOT NULL,
  `fileloc_hash` bigint(20) NOT NULL,
  `data` json DEFAULT NULL,
  `data_compressed` blob,
  `last_updated` timestamp(6) NOT NULL,
  `dag_hash` varchar(32) NOT NULL,
  `processor_subdir` varchar(2000) DEFAULT NULL,
  PRIMARY KEY (`dag_id`),
  KEY `idx_fileloc_hash` (`fileloc_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;