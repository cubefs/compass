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

# Example of version 2.0.6, you need to be consistent with the version you actually use.
------------------------------------------------
---------------- flow (dag) table ----------------------
------------------------------------------------
CREATE TABLE `t_ds_process_definition` (
   `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'self-increasing id',
   `code` bigint(20) NOT NULL COMMENT 'encoding',
   `name` varchar(255) DEFAULT NULL COMMENT 'process definition name',
   `version` int(11) DEFAULT '0' COMMENT 'process definition version',
   `description` text COMMENT 'description',
   `project_code` bigint(20) NOT NULL COMMENT 'project code',
   `release_state` tinyint(4) DEFAULT NULL COMMENT 'process definition release state：0:offline,1:online',
   `user_id` int(11) DEFAULT NULL COMMENT 'process definition creator id',
   `global_params` text COMMENT 'global parameters',
   `flag` tinyint(4) DEFAULT NULL COMMENT '0 not available, 1 available',
   `locations` text COMMENT 'Node location information',
   `warning_group_id` int(11) DEFAULT NULL COMMENT 'alert group id',
   `timeout` int(11) DEFAULT '0' COMMENT 'time out, unit: minute',
   `tenant_id` int(11) NOT NULL DEFAULT '-1' COMMENT 'tenant id',
   `execution_type` tinyint(4) DEFAULT '0' COMMENT 'execution_type 0:parallel,1:serial wait,2:serial discard,3:serial priority',
   `create_time` datetime NOT NULL COMMENT 'create time',
   `update_time` datetime NOT NULL COMMENT 'update time',
   PRIMARY KEY (`id`,`code`),
   UNIQUE KEY `process_unique` (`name`,`project_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8

------------------------------------------------
---------------- 流（dag）执行实例 ----------------------
------------------------------------------------
CREATE TABLE `t_ds_process_instance` (
     `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
     `name` varchar(255) DEFAULT NULL COMMENT 'process instance name',
     `process_definition_code` bigint(20) NOT NULL COMMENT 'process definition code',
     `process_definition_version` int(11) DEFAULT '0' COMMENT 'process definition version',
     `state` tinyint(4) DEFAULT NULL COMMENT 'process instance Status: 0 commit succeeded, 1 running, 2 prepare to pause, 3 pause, 4 prepare to stop, 5 stop, 6 fail, 7 succeed, 8 need fault tolerance, 9 kill, 10 wait for thread, 11 wait for dependency to complete',
     `recovery` tinyint(4) DEFAULT NULL COMMENT 'process instance failover flag：0:normal,1:failover instance',
     `start_time` datetime DEFAULT NULL COMMENT 'process instance start time',
     `end_time` datetime DEFAULT NULL COMMENT 'process instance end time',
     `run_times` int(11) DEFAULT NULL COMMENT 'process instance run times',
     `host` varchar(135) DEFAULT NULL COMMENT 'process instance host',
     `command_type` tinyint(4) DEFAULT NULL COMMENT 'command type',
     `command_param` text COMMENT 'json command parameters',
     `task_depend_type` tinyint(4) DEFAULT NULL COMMENT 'task depend type. 0: only current node,1:before the node,2:later nodes',
     `max_try_times` tinyint(4) DEFAULT '0' COMMENT 'max try times',
     `failure_strategy` tinyint(4) DEFAULT '0' COMMENT 'failure strategy. 0:end the process when node failed,1:continue running the other nodes when node failed',
     `warning_type` tinyint(4) DEFAULT '0' COMMENT 'warning type. 0:no warning,1:warning if process success,2:warning if process failed,3:warning if success',
     `warning_group_id` int(11) DEFAULT NULL COMMENT 'warning group id',
     `schedule_time` datetime DEFAULT NULL COMMENT 'schedule time',
     `command_start_time` datetime DEFAULT NULL COMMENT 'command start time',
     `global_params` text COMMENT 'global parameters',
     `flag` tinyint(4) DEFAULT '1' COMMENT 'flag',
     `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
     `is_sub_process` int(11) DEFAULT '0' COMMENT 'flag, whether the process is sub process',
     `executor_id` int(11) NOT NULL COMMENT 'executor id',
     `history_cmd` text COMMENT 'history commands of process instance operation',
     `process_instance_priority` int(11) DEFAULT NULL COMMENT 'process instance priority. 0 Highest,1 High,2 Medium,3 Low,4 Lowest',
     `worker_group` varchar(64) DEFAULT NULL COMMENT 'worker group id',
     `environment_code` bigint(20) DEFAULT '-1' COMMENT 'environment code',
     `timeout` int(11) DEFAULT '0' COMMENT 'time out',
     `tenant_id` int(11) NOT NULL DEFAULT '-1' COMMENT 'tenant id',
     `var_pool` longtext COMMENT 'var_pool',
     `dry_run` tinyint(4) DEFAULT '0' COMMENT 'dry run flag：0 normal, 1 dry run',
     `next_process_instance_id` int(11) DEFAULT '0' COMMENT 'serial queue next processInstanceId',
     PRIMARY KEY (`id`),
     KEY `process_instance_index` (`process_definition_code`,`id`) USING BTREE,
     KEY `start_time_index` (`start_time`,`end_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=653 DEFAULT CHARSET=utf8

------------------------------------------------
---------------- task up down stream table -------------------
------------------------------------------------
CREATE TABLE `t_ds_process_task_relation` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'self-increasing id',
  `name` varchar(200) DEFAULT NULL COMMENT 'relation name',
  `project_code` bigint(20) NOT NULL COMMENT 'project code',
  `process_definition_code` bigint(20) NOT NULL COMMENT 'process code',
  `process_definition_version` int(11) NOT NULL COMMENT 'process version',
  `pre_task_code` bigint(20) NOT NULL COMMENT 'pre task code',
  `pre_task_version` int(11) NOT NULL COMMENT 'pre task version',
  `post_task_code` bigint(20) NOT NULL COMMENT 'post task code',
  `post_task_version` int(11) NOT NULL COMMENT 'post task version',
  `condition_type` tinyint(2) DEFAULT NULL COMMENT 'condition type : 0 none, 1 judge 2 delay',
  `condition_params` text COMMENT 'condition params(json)',
  `create_time` datetime NOT NULL COMMENT 'create time',
  `update_time` datetime NOT NULL COMMENT 'update time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=238 DEFAULT CHARSET=utf8

------------------------------------------------
---------------- project -------------------
------------------------------------------------
CREATE TABLE `t_ds_project` (
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
    `name` varchar(100) DEFAULT NULL COMMENT 'project name',
    `code` bigint(20) NOT NULL COMMENT 'encoding',
    `description` varchar(200) DEFAULT NULL,
    `user_id` int(11) DEFAULT NULL COMMENT 'creator id',
    `flag` tinyint(4) DEFAULT '1' COMMENT '0 not available, 1 available',
    `create_time` datetime NOT NULL COMMENT 'create time',
    `update_time` datetime DEFAULT NULL COMMENT 'update time',
    PRIMARY KEY (`id`),
    KEY `user_id_index` (`user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8

------------------------------------------------
---------------- task definition ---------------
------------------------------------------------
CREATE TABLE `t_ds_task_definition` (
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'self-increasing id',
    `code` bigint(20) NOT NULL COMMENT 'encoding',
    `name` varchar(200) DEFAULT NULL COMMENT 'task definition name',
    `version` int(11) DEFAULT '0' COMMENT 'task definition version',
    `description` text COMMENT 'description',
    `project_code` bigint(20) NOT NULL COMMENT 'project code',
    `user_id` int(11) DEFAULT NULL COMMENT 'task definition creator id',
    `task_type` varchar(50) NOT NULL COMMENT 'task type',
    `task_params` longtext COMMENT 'job custom parameters',
    `flag` tinyint(2) DEFAULT NULL COMMENT '0 not available, 1 available',
    `task_priority` tinyint(4) DEFAULT NULL COMMENT 'job priority',
    `worker_group` varchar(200) DEFAULT NULL COMMENT 'worker grouping',
    `environment_code` bigint(20) DEFAULT '-1' COMMENT 'environment code',
    `fail_retry_times` int(11) DEFAULT NULL COMMENT 'number of failed retries',
    `fail_retry_interval` int(11) DEFAULT NULL COMMENT 'failed retry interval',
    `timeout_flag` tinyint(2) DEFAULT '0' COMMENT 'timeout flag:0 close, 1 open',
    `timeout_notify_strategy` tinyint(4) DEFAULT NULL COMMENT 'timeout notification policy: 0 warning, 1 fail',
    `timeout` int(11) DEFAULT '0' COMMENT 'timeout length,unit: minute',
    `delay_time` int(11) DEFAULT '0' COMMENT 'delay execution time,unit: minute',
    `resource_ids` text COMMENT 'resource id, separated by comma',
    `create_time` datetime NOT NULL COMMENT 'create time',
    `update_time` datetime NOT NULL COMMENT 'update time',
    PRIMARY KEY (`id`,`code`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8

------------------------------------------------
---------------- task instance table -----------
------------------------------------------------
CREATE TABLE `t_ds_task_instance` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `name` varchar(255) DEFAULT NULL COMMENT 'task name',
  `task_type` varchar(50) NOT NULL COMMENT 'task type',
  `task_code` bigint(20) NOT NULL COMMENT 'task definition code',
  `task_definition_version` int(11) DEFAULT '0' COMMENT 'task definition version',
  `process_instance_id` int(11) DEFAULT NULL COMMENT 'process instance id',
  `state` tinyint(4) DEFAULT NULL COMMENT 'Status: 0 commit succeeded, 1 running, 2 prepare to pause, 3 pause, 4 prepare to stop, 5 stop, 6 fail, 7 succeed, 8 need fault tolerance, 9 kill, 10 wait for thread, 11 wait for dependency to complete',
  `submit_time` datetime DEFAULT NULL COMMENT 'task submit time',
  `start_time` datetime DEFAULT NULL COMMENT 'task start time',
  `end_time` datetime DEFAULT NULL COMMENT 'task end time',
  `host` varchar(135) DEFAULT NULL COMMENT 'host of task running on',
  `execute_path` varchar(200) DEFAULT NULL COMMENT 'task execute path in the host',
  `log_path` varchar(200) DEFAULT NULL COMMENT 'task log path',
  `alert_flag` tinyint(4) DEFAULT NULL COMMENT 'whether alert',
  `retry_times` int(4) DEFAULT '0' COMMENT 'task retry times',
  `pid` int(4) DEFAULT NULL COMMENT 'pid of task',
  `app_link` text COMMENT 'yarn app id',
  `task_params` text COMMENT 'job custom parameters',
  `flag` tinyint(4) DEFAULT '1' COMMENT '0 not available, 1 available',
  `retry_interval` int(4) DEFAULT NULL COMMENT 'retry interval when task failed ',
  `max_retry_times` int(2) DEFAULT NULL COMMENT 'max retry times',
  `task_instance_priority` int(11) DEFAULT NULL COMMENT 'task instance priority:0 Highest,1 High,2 Medium,3 Low,4 Lowest',
  `worker_group` varchar(64) DEFAULT NULL COMMENT 'worker group id',
  `environment_code` bigint(20) DEFAULT '-1' COMMENT 'environment code',
  `environment_config` text COMMENT 'this config contains many environment variables config',
  `executor_id` int(11) DEFAULT NULL,
  `first_submit_time` datetime DEFAULT NULL COMMENT 'task first submit time',
  `delay_time` int(4) DEFAULT '0' COMMENT 'task delay execution time',
  `var_pool` longtext COMMENT 'var_pool',
  `dry_run` tinyint(4) DEFAULT '0' COMMENT 'dry run flag: 0 normal, 1 dry run',
  PRIMARY KEY (`id`),
  KEY `process_instance_id` (`process_instance_id`) USING BTREE,
  CONSTRAINT `foreign_key_instance_id` FOREIGN KEY (`process_instance_id`) REFERENCES `t_ds_process_instance` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=667 DEFAULT CHARSET=utf8

------------------------------------------------
---------------- user table -------------------
------------------------------------------------
CREATE TABLE `t_ds_user` (
     `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'user id',
     `user_name` varchar(64) DEFAULT NULL COMMENT 'user name',
     `user_password` varchar(64) DEFAULT NULL COMMENT 'user password',
     `user_type` tinyint(4) DEFAULT NULL COMMENT 'user type, 0:administrator，1:ordinary user',
     `email` varchar(64) DEFAULT NULL COMMENT 'email',
     `phone` varchar(11) DEFAULT NULL COMMENT 'phone',
     `tenant_id` int(11) DEFAULT NULL COMMENT 'tenant id',
     `create_time` datetime DEFAULT NULL COMMENT 'create time',
     `update_time` datetime DEFAULT NULL COMMENT 'update time',
     `queue` varchar(64) DEFAULT NULL COMMENT 'queue',
     `state` tinyint(4) DEFAULT '1' COMMENT 'state 0:disable 1:enable',
     PRIMARY KEY (`id`),
     UNIQUE KEY `user_name_unique` (`user_name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8
