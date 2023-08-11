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
   `start_time` timestamp(6) NULL DEFAULT NULL COMMENT '任务开始时间',
   `end_time`   timestamp(6) NULL DEFAULT NULL COMMENT '任务结束时间',
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
  `parent_action` varchar(64) DEFAULT NULL COMMENT '父节点异常事件',
  `action` varchar(64) DEFAULT NULL COMMENT '异常事件',
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
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='任务基线运行表';

-- ----------------------------
-- Flink: Table structure for flink_app
-- ----------------------------
 CREATE TABLE `flink_task` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '实时任务id',
  `username` varchar(64) DEFAULT NULL COMMENT '用户名',
  `user_id` int(11) DEFAULT NULL COMMENT '用户记录id',
  `project_name` varchar(64) DEFAULT NULL COMMENT '项目名',
  `project_id` int(11) DEFAULT NULL COMMENT '项目id',
  `flow_name` varchar(64) DEFAULT NULL COMMENT '工作流名称',
  `flow_id` int(11) DEFAULT NULL COMMENT '工作流id',
  `task_name` varchar(64) DEFAULT NULL COMMENT '任务名称',
  `task_id` int(11) DEFAULT NULL COMMENT '任务id',
  `deleted` int(11) NOT NULL DEFAULT '0' COMMENT '是否删除',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_project_flow_task` (`project_name`,`flow_name`,`task_name`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8 COMMENT='实时任务表'

-- ----------------------------
-- Flink: Table structure for flink_task_app
-- ----------------------------
 CREATE TABLE `flink_task_app` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '实时任务id',
  `username` varchar(64) DEFAULT NULL COMMENT '用户名',
  `user_id` int(11) DEFAULT NULL COMMENT '用户记录id',
  `project_name` varchar(64) DEFAULT NULL COMMENT '项目名',
  `project_id` int(11) DEFAULT NULL COMMENT '项目id',
  `flow_name` varchar(64) DEFAULT NULL COMMENT '工作流名称',
  `flow_id` int(11) DEFAULT NULL COMMENT '工作流id',
  `task_name` varchar(64) DEFAULT NULL COMMENT '任务名称',
  `task_id` int(11) DEFAULT NULL COMMENT '任务id',
  `task_state` varchar(64) DEFAULT NULL COMMENT '任务运行状态,running,finish',
  `task_instance_id` int(11) NOT NULL COMMENT 'task instance id',
  `execution_time` datetime DEFAULT NULL COMMENT 'task instance 执行周期',
  `application_id` varchar(64) DEFAULT NULL COMMENT 'appId',
  `flink_track_url` varchar(255) DEFAULT NULL COMMENT 'flink track url',
  `allocated_mb` int(11) DEFAULT NULL COMMENT 'yarn获取的总共分配mb',
  `allocated_vcores` int(11) DEFAULT NULL COMMENT 'yarn获取的总共分配vcore',
  `running_containers` int(11) DEFAULT NULL COMMENT 'yarn获取的总共分配容器',
  `engine_type` varchar(64) DEFAULT NULL COMMENT '执行引擎',
  `duration` double DEFAULT NULL COMMENT '运行耗时',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `vcore_seconds` float DEFAULT NULL COMMENT 'cpu消耗(vcore-seconds)',
  `memory_seconds` float DEFAULT NULL COMMENT '内存消耗(GB-seconds)',
  `queue` varchar(64) DEFAULT NULL COMMENT '运行队列',
  `cluster_name` varchar(64) DEFAULT NULL COMMENT '集群名称',
  `retry_times` int(11) DEFAULT NULL COMMENT '重试次数',
  `execute_user` varchar(64) DEFAULT NULL COMMENT '执行用户',
  `diagnosis` varchar(255) COMMENT 'yarn诊断信息',
  `parallel` int(11) DEFAULT NULL COMMENT 'flink 并行度',
  `tm_slot` int(11) DEFAULT NULL COMMENT 'flink tm slot',
  `tm_core` int(11) DEFAULT NULL COMMENT 'flink tm core',
  `tm_mem` int(11) DEFAULT NULL COMMENT 'flink tm_mem',
  `jm_mem` int(11) DEFAULT NULL COMMENT 'flink jm_mem',
  `job_name` varchar(255) COMMENT 'job name',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_application_id` (`application_id`),
  KEY `idx_project_name` (`project_name`),
  KEY `idx_flow_name` (`flow_name`),
  KEY `idx_task_name` (`task_name`),
  KEY `idx_username` (`username`),
  KEY `idx_job_name` (`job_name`),
  KEY `idx_task_state` (`task_state`)
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8 COMMENT='实时任务application表'

-- ----------------------------
-- Flink: Table structure for flink_task_diagnosis_rule_advice
-- ----------------------------
 CREATE TABLE `flink_task_diagnosis_rule_advice` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '实时任务诊断规则结果id',
  `flink_task_diagnosis_id` int(11) DEFAULT NULL,
  `rule_name` varchar(255) DEFAULT NULL COMMENT '规则名',
  `rule_type` int(11) DEFAULT NULL COMMENT '规则编码',
  `has_advice` smallint(1) unsigned DEFAULT '0' COMMENT '规则是否命中0未1有',
  `description` varchar(512) DEFAULT NULL COMMENT '诊断规则描述',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_realtime_task_diagnosis_id` (`flink_task_diagnosis_id`)
) ENGINE=InnoDB AUTO_INCREMENT=236082 DEFAULT CHARSET=utf8 COMMENT='实时任务诊断规则结果表'


INSERT INTO `user` (`username`, `password`) values ('compass', 'compass');

INSERT INTO `task_diagnosis_advice` VALUES (1,'driver',NULL,'containerFailed','Container失败','{container}失败，诊断信息为{diagnostics}，退出码为{exitCode}',NULL,'container,diagnostic,exitCode','otherException',0,NULL),(2,'driver','','stageFailed','Stage失败','Stage{stage}连续{failedNum}次失败，此job彻底失败',NULL,'stage,failedNum','otherException',0,NULL),(3,'driver','','jobFailedOrAbortedException','任务失败或退出异常','',NULL,NULL,'otherException',0,NULL),(4,'driver','jobFailedOrAbortedException','fileNotFoundException','写入数据失败，文件不存在','可能存在以下情况：<br/>1、可能存在并发任务操作同一路径导致任务写入失败，建议重试任务<br/>2、可能存在来源表数据被更新，请在来源表更新结束后再重新执行任务<br/>3、表分区存在，但分区路径不存在报错。因为元数据数据还存在，导致元数据去指明路径时路径不存在故报错。建议在删除分区时，用drop分区，这样才会删除数据和分区。',NULL,NULL,'sqlFailed',0,NULL),(5,'driver','jobFailedOrAbortedException','failedRunJobNoTablePermission','用户没有权限','用户({user})没有对({database}/{table})库表的{option}权限。<a target=\"_blank\" style=\"color: rgb(45, 204, 195);\" href=\"\">请先申请相应权限</a>',NULL,'user,database,table','sqlFailed',0,NULL),(6,'driver','jobFailedOrAbortedException','sqlSyncError','sql语法错误','外部表必须添加外部地址',NULL,NULL,'sqlFailed',0,NULL),(7,'driver','jobFailedOrAbortedException','broadcastsTimeout','广播超时','经引擎判断需要广播（自动或者用户强制hint）的表或者子查询的数据需要在{threshold}秒内完成广播至各计算节点',NULL,'threshold','otherException',0,NULL),(8,'driver','jobFailedOrAbortedException','blockMissingException','块丢失异常','请提供以下报错并联系技术支持。<br/> BlockMissingException: Could not obtain block:xxx',NULL,NULL,'otherException',0,NULL),(9,'driver','jobFailedOrAbortedException','outOfHeapOOM','shuffle阶段获取数据时堆外内存溢出','建议使用OPPO shuttle服务，开源项目：https://github.com/cubefs/shuttle',NULL,NULL,'memoryOverflow',0,NULL),(10,'driver',NULL,'shuffleFetchFailed','shuffle连接不上',NULL,NULL,NULL,'shuffleFailed',0,NULL),(11,'driver',NULL,'otherError','其他错误信息',NULL,NULL,NULL,'otherException',0,NULL),(12,'executor',NULL,'shuffleBlockFetcherIterator','节点shuffle负载过高','建议使用OPPO shuttle服务，开源项目：https://github.com/cubefs/shuttle',NULL,NULL,'shuffleFailed',0,NULL),(13,'executor',NULL,'connectorOfExecutorAndDriverException1','executor与driver节点通讯异常',NULL,NULL,NULL,'otherException',0,NULL),(14,'executor',NULL,'connectorOfExecutorAndDriverException2','executor与driver节点通讯异常',NULL,NULL,NULL,'otherException',0,NULL),(15,'executor',NULL,'connectorOfExecutorAndDriverException3','executor与driver节点通讯异常',NULL,NULL,NULL,'otherException',0,NULL),(16,'executor',NULL,'noLzo','机器中无lzo包','请提供任务执行的机器并联系技术支持查看机器lzo包配置。',NULL,NULL,'otherException',0,NULL),(17,'executor',NULL,'diskBlockObjectWriterError','写入磁盘失败，可能存在磁盘损坏的情况','如重试任务依然失败，且同一个报错，请联系技术支持并提供报错查看问题。',NULL,NULL,'otherException',0,NULL),(18,'executor',NULL,'transportChannelHandler','请求超时','该类错误未分类',NULL,NULL,'otherException',0,'未检测到异常'),(19,'executor',NULL,'otherError','其他错误信息',NULL,NULL,NULL,'otherException',0,'未检测到异常'),(20,'event',NULL,'largeTableScan','大表扫描','表{tables}的扫描量{values}，超过阈值{threshold}行，发生大表扫描。</br>该任务存在情况：<br/>1.大表或全表扫描的情况，大表扫描或者全表扫描耗时长、浪费资源，也会给集群带来一定压力，且容易导致内存溢出任务失败，建议检查执行sql ，确认逻辑，添加分区条件，过滤不需要的数据，避免不必要全表扫描；<br/>2.多次扫描大表的情况，建议将所有的目标数据进行一次大表扫描收集到临时表（或者通过global temp view缓存到内存中，视数据量而写，一般小于几十G的数据量可以缓存），从而将N次扫描大表转换为1次扫描大表+N次扫描小表，减少大消耗的操作。',NULL,'tables,threshold,values','largeTableScan',0,'未检测到异常'),(21,'event',NULL,'cpuWaste','CPU浪费','app资源总消耗:{appConsume},driver资源浪费:{driverWaste} 占比:{driverWastedPercent},executor资源浪费:{executorWaste} 占比:{executorWastedPercent}。计算资源存在浪费,请适当减小executor的并发数,优化任务。',NULL,' appConsume,driverWastedPercent,executorWastedPercent,executorWaste,driverWaste','cpuWaste',0,'未检测到异常'),(22,'event',NULL,'dataSkew','数据倾斜',' {dataSkewInfo}，读取的数据量严重超过Stage下的中位值，发生数据倾斜。<br/> 具体解决方法可参考如下建议：<br/>1、找到倾斜阶段对应执行的sql<br/>2、查看关联字段是否有倾斜（join on，group by，partition by 的字段）<br/>3、根据任务需要，如果异常值不需要可以过滤（字段！=异常值)；如果异常值也需要，可以单独处理（第一段sql排除异常值，union all 只有异常值的数据）<br/>',NULL,'dataSkewInfo','dataSkew',0,'未检测到异常'),(23,'event',NULL,'globalSortAbnormal','全局排序异常','{globalSortInfo}, 发生全局排序异常，请联系技术支持优化代码。',NULL,'globalSortInfo','globalSortAbnormal',0,'未检测到异常'),(24,'event',NULL,'hdfsStuck','HDFS卡顿','{hdfsSlowInfo}, 发生HDFS卡顿，请及时关注HDFS集群负载情况。',NULL,'hdfsStuckInfo','hdfsStuck',0,'未检测到异常'),(25,'event',NULL,'jobDurationAbnormal','Job耗时异常','{jobs}空闲时间与总时间的占比为：{values}，超过阈值{threshold}，发生job耗时异常。请及时关注平台运行状态。',NULL,'jobs,values,threshold','jobDurationAbnormal',0,'未检测到异常'),(26,'event',NULL,'memoryWaste','内存浪费',' Driver分配内存{driverMemory},峰值内存{driverPeak},Executor分配内存{executorMemory},峰值内存{executorPeak}。结合运行时间计算内存浪费为{wasteRatio},超过阈值{threshold}, 建议适当减少driver/executor的内存大小，优化成本',NULL,' driverMemory,driverPeak,executorMemory,executorPeak,threshold,wasteRatio','memoryWaste',0,'未检测到异常'),(27,'event',NULL,'oom','OOM预警','执行sql中广播内存占比：{scanTable}与{execType}内存占比={usePercent}%<br/>该任务被广播的表与driver或executor任意一个内存占比已超过阈值{oom}%，存在OOM内存溢出风险，建议提前增加相应内存，禁用广播或取消强制广播。<br/>禁用广播参数为：--hiveconf livy.session.conf.spark.sql.autoBroadcastJoinThreshold=-1;<br/>增加内存参数为（增加当前内存的20%）：（executor内存） --hiveconf livy.session.conf.spark.executor.memory;（driver内存）--hiveconf livy.session.conf.spark.driver.memory;<br/>如果非强制广播的情况下，spark2对是否广播判断有一定概率失误，建议切换到spark3。',NULL,'scanTable,execType,usePercent,oom','oomWarn',0,'未检测到异常'),(28,'event',NULL,'speculativeTask','推测执行过多','{jobStages} 推测执行数量为：{values}，超过阈值{threshold}个，推测执行过多，请联系技术支持优化代码。',NULL,'jobStages,values,threshold','speculativeTask',0,'未检测到异常'),(29,'event',NULL,'stageDurationAbnormal','Stage耗时异常','{jobStages}空闲时间与该Stage总时间的占比为：{values}，超过阈值{threshold}，该stage耗时异常。请及时关注平台运行状态。',NULL,'jobStages,values,threshold','stageDurationAbnormal',0,'未检测到异常'),(30,'event',NULL,'taskDurationAbnormal','Task耗时异常','{taskDurationInfo}，任务的运行时间远远大于中位值，发生Task耗时异常。',NULL,'taskDurationInfo,reason','taskDurationAbnormal',0,'未检测到异常'),(31,'yarn',NULL,'driverOOM','内存溢出','内存溢出导致任务失败，退出码：137。\\n建议调整参数：\\n增加driver内存，增大为现有内存的30%：\\n--conf spark.driver.memory  \\n一般driver内存溢出有两种情况可能发生，一种是读取的文件数量过多，另一种是广播过大数据。第一种情况可以查看是否充分利用分区条件，或者减小数据量，分段执行sql ；第二种情况可以禁用广播，或者取消强制广播。这两种情况都可以通过增加driver内存临时解决。','Application application_.* failed 1 times due to AM Container for appattempt_.*exitCode: 137.*$',NULL,'memoryOverflow',0,'未检测到异常'),(32,'yarn',NULL,'fileAlreadyExistsException','路径已存在','{filePath}写入路径已存在，建议：可检查代码逻辑，在代码逻辑中添加判断output文件夹是否存在，如果存在则删除。','User class threw exception: org.apache.hadoop.mapred.FileAlreadyExistsException: Output directory (?<filePath>.+) already exists.*$','filePath','otherException',0,'未检测到异常'),(33,'yarn',NULL,'killByHiveServer','任务异常终止','{job}任务被hiveserver(机器ip:{ip})终止','Kill job (?<job>.+) received from hive.*at (?<ip>.+) Job received Kill while in RUNNING state.*$','job,ip','otherException',0,'未检测到异常'),(34,'yarn',NULL,'killedByUser',' 任务异常终止','任务被手动终止','Application killed by user.*$',NULL,'otherException',0,'未检测到异常'),(35,'yarn',NULL,'mysqlPermissionDenied','mysql权限缺失','用户名:{user}@机器ip:{ip}没有mysql的访问权限，请添加相应的mysql权限后，再重试任务。','User class threw exception:.*: Access denied for user \'(?<user>.+)\'@\'(?<ip>.+)\'.*$',' user,ip','sqlFailed',0,'未检测到异常'),(36,'yarn',NULL,'otherError','AM异常','该类错误未分类','.*',NULL,'otherException',0,'未检测到异常'),(37,'yarn',NULL,'permissionDenied','权限缺失','用户{user}没有{inode}库表的{access}权限,建议先申请权限再重试。','User class threw exception.*AccessControlException.*user=(?<user>.+), access=(?<access>.+), inode=\\\"(?<inode>.+)\\\".*$','user,access,inode','sqlFailed',0,'未检测到异常'),(38,'yarn',NULL,'systemTimesError','时间同步异常','服务器时间同步问题，建议任务重试','.*System times on machines may be out of sync.*$',NULL,'otherException',0,'未检测到异常'),(39,'scheduler',NULL,'otherError','其他错误信息','该类错误未分类',NULL,NULL,'otherException',0,'未检测到异常'),(40,'event',NULL,'broadcastOOM','广播过滤行数过多','执行sql中广播过滤行数：{maxRows} ,内存占比：{usePercent}%。<br/>该任务广播过滤行数超过{broadcastRows}且该任务被广播的表与driver或executor任意一个内存占比已超过阈值{broadcastRowsOom}%，存在OOM内存溢出风险，建议提前增加相应内存，禁用广播或取消强制广播。<br/>禁用广播参数为：--hiveconf livy.session.conf.spark.sql.autoBroadcastJoinThreshold=-1;<br/>增加内存参数为（增加当前内存的20%）：（executor内存） --hiveconf livy.session.conf.spark.executor.memory;（driver内存）--hiveconf livy.session.conf.spark.driver.memory;<br/>如果非强制广播的情况下，spark2对是否广播判断有一定概率失误，建议切换到spark3。',NULL,'maxRows,usePercent,broadcastRows,broadcastRowsOom','oomWarn',0,'未检测到异常'),(41,'driver',NULL,'outOfMemoryError','内存溢出','适当增加driver内存',NULL,NULL,'memoryOverflow',0,NULL);
