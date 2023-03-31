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

CREATE DATABASE IF NOT EXISTS compass;
USE compass;

-- ----------------------------
-- Table structure for user
-- ----------------------------
CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '用户记录id',
  `user_id` int(11) NOT NULL DEFAULT '0' COMMENT '用户id,跟其他系统保持一致',
  `username` varchar(64) DEFAULT NULL COMMENT '用户名',
  `password` varchar(256) DEFAULT NULL COMMENT '用户密码',
  `is_admin` int(1) DEFAULT '0' COMMENT '是否为管理员',
  `icon` varchar(500) DEFAULT NULL COMMENT '头像',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(64) DEFAULT NULL COMMENT '电话',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `status` int(1) DEFAULT '1' COMMENT '账号启用状态: 0->禁用; 1->启用',
  `scheduler_type` varchar(64) DEFAULT NULL COMMENT '调度器类型',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_username` (`username`,`scheduler_type`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户表';

-- ----------------------------
-- Table structure for project
-- ----------------------------
CREATE TABLE `project` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '项目id',
  `project_name` varchar(64) DEFAULT NULL COMMENT '项目名',
  `description` varchar(2048) DEFAULT NULL COMMENT '项目描述',
  `user_id` int(11) DEFAULT NULL COMMENT '创建者id',
  `project_status` int(1) DEFAULT '1' COMMENT '项目启用状态: 0->禁用; 1->启用',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_project` (`project_name`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT '项目表';

-- ----------------------------
-- Table structure for flow （dag/process）
-- ----------------------------
CREATE TABLE `flow` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '工作流id',
  `flow_name` varchar(64) DEFAULT NULL COMMENT '工作流名称',
  `description` varchar(2048) DEFAULT NULL COMMENT '工作流描述',
  `user_id` int(11) DEFAULT NULL COMMENT '工作流创建者id',
  `flow_status` int(1) DEFAULT '1' COMMENT '工作流启用状态: 0->禁用; 1->启用',
  `project_id` int(11) DEFAULT NULL COMMENT '工作流所属项目id',
  `project_name` varchar(64) DEFAULT NULL COMMENT '项目名',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_flow` (`flow_name`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT '工作流表';

-- ----------------------------
-- Table structure for task
-- ----------------------------
CREATE TABLE `task` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '任务id',
  `project_name` varchar(64) DEFAULT NULL COMMENT '项目名',
  `project_id` int(11) DEFAULT NULL COMMENT '项目记录id',
  `flow_name` varchar(64) DEFAULT NULL COMMENT '工作流名称',
  `flow_id`  int(11) DEFAULT NULL COMMENT '工作流记录id',
  `task_name` varchar(64) DEFAULT NULL COMMENT '任务名称',
  `description` varchar(2048) DEFAULT NULL COMMENT '任务描述',
  `user_id` int(11) DEFAULT NULL COMMENT '任务创建者id',
  `task_type` varchar(50) DEFAULT NULL COMMENT '任务类型: SHELL, PYTHON, SPARK',
  `retries` int(11) DEFAULT NULL COMMENT '失败重试次数',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT '任务表';

-- ----------------------------
-- Table structure for task_instance
-- ----------------------------
CREATE TABLE `task_instance`(
   `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '任务执行id',
   `project_name` varchar(64) DEFAULT NULL COMMENT '项目名',
   `flow_name` varchar(64) DEFAULT NULL COMMENT '工作流名称',
   `task_name` varchar(64) DEFAULT NULL COMMENT '任务名称',
   `execution_time` datetime(6) DEFAULT NULL COMMENT '任务执行周期',
   `start_time` timestamp(6) DEFAULT NULL COMMENT '任务开始时间',
   `end_time`   timestamp(6) DEFAULT NULL COMMENT '任务结束时间',
   `task_state` varchar(64) DEFAULT NULL COMMENT '任务执行状态',
   `task_type`  varchar(64) DEFAULT NULL COMMENT '任务类型',
   `retry_times` int(11) DEFAULT NULL COMMENT '任务重试第n次',
   `max_retry_times` int(2) DEFAULT NULL COMMENT '任务执行最大重试次数',
   `worker_group` varchar(64) DEFAULT NULL COMMENT '任务执行的work group',
   `trigger_type` varchar(32) DEFAULT NULL COMMENT '任务触发类型：manual(手动触发), schedule(调触发)',
   `create_time` datetime DEFAULT NULL COMMENT '创建时间',
   `update_time` datetime DEFAULT NULL COMMENT '更新时间',
   PRIMARY KEY (`id`),
   UNIQUE KEY `idx_project_flow_task_execution_date_retry` (`project_name`, `flow_name`, `task_name`, `execution_time`, `retry_times`),
   KEY `idx_flow_task_execution` (`flow_name`,`task_name`,`execution_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT '任务执行表';

-- ----------------------------
-- Table structure for task_application
-- ----------------------------
CREATE TABLE `task_application` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '异常任务id',
  `application_id` varchar(64) DEFAULT NULL COMMENT 'appId',
  `task_name` varchar(64) DEFAULT NULL COMMENT '任务名称',
  `flow_name` varchar(64) DEFAULT NULL COMMENT '工作流名称',
  `project_name` varchar(64) DEFAULT NULL COMMENT '项目名称',
  `execute_time` datetime DEFAULT NULL COMMENT '任务计划执行时间',
  `retry_times` int(2) DEFAULT NULL COMMENT '任务重试第n次',
  `log_path` text DEFAULT NULL COMMENT '任务调度器日志,多个用逗号隔开',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_project_flow_task_appId` (`project_name`,`flow_name`,`task_name`,`execute_time`,`application_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='任务application表';

-- ----------------------------
-- Table structure for task_blocklist
-- ----------------------------
CREATE TABLE `blocklist` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '任务白名单id',
  `component` varchar(64) DEFAULT NULL COMMENT '组件',
  `project_name` varchar(64) DEFAULT NULL COMMENT '项目名称',
  `flow_name` varchar(64) DEFAULT NULL COMMENT '工作流名称',
  `task_name` varchar(64) DEFAULT NULL COMMENT '任务名称',
  `username` varchar(64) DEFAULT NULL COMMENT '添加用户',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleted` int(11) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_project_flow_task_category` (`project_name`,`flow_name`,`task_name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='任务白名单表';


-- ----------------------------
-- Table structure for task_syncer_init
-- ----------------------------
CREATE TABLE `task_syncer_init` (
    is_init int(11) NOT NULL DEFAULT '0' COMMENT 'task_syncer应用是否已初始化： 0 -> 否, 1 -> 是',
    UNIQUE KEY `idx_is_init` (`is_init`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'task-syncer应用初始化表';


-- ----------------------------
-- Table structure for task_diagnose_advice
-- ----------------------------
CREATE TABLE `task_diagnosis_advice` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `log_type` varchar(64) DEFAULT NULL COMMENT '日志类型',
  `action` varchar(64) DEFAULT NULL COMMENT '异常事件',
  `parent_action` varchar(64) DEFAULT NULL COMMENT '父节点异常事件',
  `description` varchar(255) DEFAULT NULL COMMENT '异常事件描述',
  `abnormal_advice` text COMMENT '建议（其中的变量用 {变量名} 表示）',
  `rule` text COMMENT '匹配规则',
  `variables` varchar(255) DEFAULT NULL COMMENT '变量名列表( , 为分割符)',
  `category` varchar(255) DEFAULT NULL COMMENT '异常类型',
  `deleted` int(11) DEFAULT '0' COMMENT '是否删除',
  `normal_advice` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_logType_action` (`log_type`,`action`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='诊断建议';

-- ----------------------------
-- Table structure for task_datum
-- ----------------------------
CREATE TABLE `task_datum` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '任务执行id',
  `project_name` varchar(64) DEFAULT NULL COMMENT '区域',
  `flow_name` varchar(180) DEFAULT NULL,
  `task_name` varchar(180) DEFAULT NULL,
  `execution_date` datetime DEFAULT NULL COMMENT '执行周期',
  `baseline` text COMMENT '基线树',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_projectName_flowName_taskName_executionDate` (`project_name`,`flow_name`,`task_name`,`execution_date`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='任务基线运行表'