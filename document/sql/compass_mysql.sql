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
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'User record id',
  `user_id` int(11) NOT NULL DEFAULT '0' COMMENT 'User ID, consistent with other systems',
  `username` varchar(64) DEFAULT NULL COMMENT 'Username',
  `password` varchar(256) DEFAULT NULL COMMENT 'Password',
  `is_admin` int(1) DEFAULT '0' COMMENT 'Is it an administrator',
  `icon` varchar(500) DEFAULT NULL COMMENT 'Icon: avatar',
  `email` varchar(100) DEFAULT NULL COMMENT 'Email',
  `phone` varchar(64) DEFAULT NULL COMMENT 'Phone',
  `create_time` datetime DEFAULT NULL COMMENT 'Create time',
  `update_time` datetime DEFAULT NULL COMMENT 'Update time',
  `login_time` datetime DEFAULT NULL COMMENT 'Last login time',
  `status` int(1) DEFAULT '1' COMMENT 'Account activation status: 0->disabled; 1->enabled',
  `scheduler_type` varchar(64) DEFAULT NULL COMMENT 'scheduler type: dolphin, airflow, etc',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_username` (`username`,`scheduler_type`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='User table';

-- ----------------------------
-- Table structure for project
-- ----------------------------
CREATE TABLE `project` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Project id',
  `project_name` varchar(64) DEFAULT NULL COMMENT 'Project name',
  `description` varchar(2048) DEFAULT NULL COMMENT 'Description',
  `user_id` int(11) DEFAULT NULL COMMENT 'Creator id',
  `project_status` int(1) DEFAULT '1' COMMENT 'Project enable status: 0->disabled; 1->enabled',
  `create_time` datetime DEFAULT NULL COMMENT 'Create time',
  `update_time` datetime DEFAULT NULL COMMENT 'Update time',
  PRIMARY KEY (`id`),
  KEY `idx_project` (`project_name`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Project table';

-- ----------------------------
-- Table structure for flow （dag/process）
-- ----------------------------
CREATE TABLE `flow` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Flow id',
  `flow_name` varchar(64) DEFAULT NULL COMMENT 'Flow name',
  `description` varchar(2048) DEFAULT NULL COMMENT 'Description',
  `user_id` int(11) DEFAULT NULL COMMENT 'Flow creator id',
  `flow_status` int(1) DEFAULT '1' COMMENT 'Flow enable status: 0->disabled; 1->enabled',
  `project_id` int(11) DEFAULT NULL COMMENT 'Project id',
  `project_name` varchar(64) DEFAULT NULL COMMENT 'Project name',
  `create_time` datetime DEFAULT NULL COMMENT 'Create time',
  `update_time` datetime DEFAULT NULL COMMENT 'Update time',
  PRIMARY KEY (`id`),
  KEY `idx_flow` (`flow_name`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Flow table';

-- ----------------------------
-- Table structure for task
-- ----------------------------
CREATE TABLE `task` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'task id',
  `project_name` varchar(64) DEFAULT NULL COMMENT 'Project name',
  `project_id` int(11) DEFAULT NULL COMMENT 'Project id',
  `flow_name` varchar(64) DEFAULT NULL COMMENT 'Flow name',
  `flow_id`  int(11) DEFAULT NULL COMMENT 'Flow id',
  `task_name` varchar(64) DEFAULT NULL COMMENT 'Task name',
  `description` varchar(2048) DEFAULT NULL COMMENT 'Description',
  `user_id` int(11) DEFAULT NULL COMMENT 'Task creator id',
  `task_type` varchar(50) DEFAULT NULL COMMENT 'Task type: SHELL, PYTHON, SPARK',
  `retries` int(11) DEFAULT NULL COMMENT 'Number of failed retries',
  `create_time` datetime DEFAULT NULL COMMENT 'Create time',
  `update_time` datetime DEFAULT NULL COMMENT 'Update time',
   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Task table';

-- ----------------------------
-- Table structure for task_instance
-- ----------------------------
CREATE TABLE `task_instance`(
   `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Task instance id',
   `project_name` varchar(64) DEFAULT NULL COMMENT 'Project name',
   `flow_name` varchar(64) DEFAULT NULL COMMENT 'Flow name',
   `task_name` varchar(64) DEFAULT NULL COMMENT 'Task name',
   `execution_time` datetime(6) DEFAULT NULL COMMENT 'Task execution cycle',
   `start_time` timestamp(6) NULL DEFAULT NULL COMMENT 'Task start time',
   `end_time`   timestamp(6) NULL DEFAULT NULL COMMENT 'Task end time',
   `task_state` varchar(64) DEFAULT NULL COMMENT 'Task state',
   `task_type`  varchar(64) DEFAULT NULL COMMENT 'Task type',
   `retry_times` int(11) DEFAULT NULL COMMENT 'Task retry nth time',
   `max_retry_times` int(2) DEFAULT NULL COMMENT 'Maximum number of retries for task execution',
   `worker_group` varchar(64) DEFAULT NULL COMMENT 'The work group where the task is executed',
   `trigger_type` varchar(32) DEFAULT NULL COMMENT 'Task trigger type: manual (manual trigger), schedule (scheduled trigger)',
   `create_time` datetime DEFAULT NULL COMMENT 'Create time',
   `update_time` datetime DEFAULT NULL COMMENT 'Update time',
   PRIMARY KEY (`id`),
   UNIQUE KEY `idx_project_flow_task_execution_date_retry` (`project_name`, `flow_name`, `task_name`, `execution_time`, `retry_times`),
   KEY `idx_flow_task_execution` (`flow_name`,`task_name`,`execution_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Task instance table';

-- ----------------------------
-- Table structure for task_application
-- ----------------------------
CREATE TABLE `task_application` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Task application record id',
  `application_id` varchar(64) DEFAULT NULL COMMENT 'AppId(yarn application)',
  `task_name` varchar(64) DEFAULT NULL COMMENT 'Task name',
  `flow_name` varchar(64) DEFAULT NULL COMMENT 'Flow name',
  `project_name` varchar(64) DEFAULT NULL COMMENT 'Project name',
  `task_type` varchar(32) DEFAULT NULL COMMENT 'Task type(Spark、Flink)',
  `execute_time` datetime DEFAULT NULL COMMENT 'Task plan execution time',
  `retry_times` int(2) DEFAULT NULL COMMENT 'Task retry nth time',
  `log_path` text DEFAULT NULL COMMENT 'Task scheduler log, multiple separated by commas',
  `create_time` datetime DEFAULT NULL COMMENT 'Create time',
  `update_time` datetime DEFAULT NULL COMMENT 'Update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_project_flow_task_appId` (`project_name`,`flow_name`,`task_name`,`execute_time`,`application_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='Task application table';

-- ----------------------------
-- Table structure for task_blocklist
-- ----------------------------
CREATE TABLE `blocklist` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Block list id',
  `component` varchar(64) DEFAULT NULL COMMENT 'Component(Spark, Flink etc)',
  `project_name` varchar(64) DEFAULT NULL COMMENT 'Project name',
  `flow_name` varchar(64) DEFAULT NULL COMMENT 'Flow name',
  `task_name` varchar(64) DEFAULT NULL COMMENT 'Task name',
  `username` varchar(64) DEFAULT NULL COMMENT 'Username',
  `create_time` datetime DEFAULT NULL COMMENT 'Create time',
  `update_time` datetime DEFAULT NULL COMMENT 'Update time',
  `deleted` int(11) NOT NULL DEFAULT '0' COMMENT 'Is deleted',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_project_flow_task_component` (`project_name`,`flow_name`,`task_name`,`component`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='Task block list table';


-- ----------------------------
-- Table structure for task_syncer_init
-- ----------------------------
CREATE TABLE `task_syncer_init` (
    is_init int(11) NOT NULL DEFAULT '0' COMMENT 'Whether the task_syncer application has been initialized: 0 -> No, 1 -> Yes',
    UNIQUE KEY `idx_is_init` (`is_init`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'task-syncer application initialization table';


-- ----------------------------
-- Table structure for task_diagnose_advice
-- ----------------------------
CREATE TABLE `task_diagnosis_advice` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `log_type` varchar(64) DEFAULT NULL COMMENT 'Log type',
  `parent_action` varchar(64) DEFAULT NULL COMMENT 'Parent node exception event',
  `action` varchar(64) DEFAULT NULL COMMENT 'Exceptional event',
  `description` varchar(255) DEFAULT NULL COMMENT 'Exceptional description',
  `abnormal_advice` text COMMENT 'Advice (variables are represented by {variable name})',
  `rule` text COMMENT 'Matching rule',
  `variables` varchar(255) DEFAULT NULL COMMENT 'Variable name list ( , is the delimiter)',
  `category` varchar(255) DEFAULT NULL COMMENT 'Exception type',
  `deleted` int(11) DEFAULT '0' COMMENT 'Is deleted',
  `normal_advice` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_logType_action` (`log_type`,`action`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='Task advice table';

-- ----------------------------
-- Table structure for task_datum
-- ----------------------------
CREATE TABLE `task_datum` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `project_name` varchar(64) DEFAULT NULL COMMENT 'Project name',
  `flow_name` varchar(180) DEFAULT NULL COMMENT 'Flow name',
  `task_name` varchar(180) DEFAULT NULL COMMENT 'Task name',
  `execution_date` datetime DEFAULT NULL COMMENT 'Execution cycle',
  `baseline` text COMMENT 'Baseline tree',
  `create_time` datetime DEFAULT NULL COMMENT 'Create time',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_projectName_flowName_taskName_executionDate` (`project_name`,`flow_name`,`task_name`,`execution_date`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='Task baseline run table';

-- ----------------------------
-- Flink: Table structure for flink_app
-- ----------------------------
 CREATE TABLE `flink_task` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `username` varchar(64) DEFAULT NULL COMMENT 'Username',
  `user_id` int(11) DEFAULT NULL COMMENT 'User id',
  `project_name` varchar(64) DEFAULT NULL COMMENT 'Project name',
  `project_id` int(11) DEFAULT NULL COMMENT 'Project id',
  `flow_name` varchar(64) DEFAULT NULL COMMENT 'Flow name',
  `flow_id` int(11) DEFAULT NULL COMMENT 'Flow id',
  `task_name` varchar(64) DEFAULT NULL COMMENT 'Task name',
  `task_id` int(11) DEFAULT NULL COMMENT 'Task id',
  `deleted` int(11) NOT NULL DEFAULT '0' COMMENT 'Is deleted',
  `create_time` datetime DEFAULT NULL COMMENT 'Create time',
  `update_time` datetime DEFAULT NULL COMMENT 'Update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_project_flow_task` (`project_name`,`flow_name`,`task_name`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8 COMMENT='Flink task metadata table';

-- ----------------------------
-- Flink: Table structure for flink_task_app
-- ----------------------------
 CREATE TABLE `flink_task_app` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `username` varchar(64) DEFAULT NULL COMMENT 'Username',
  `user_id` int(11) DEFAULT NULL COMMENT 'User id',
  `project_name` varchar(64) DEFAULT NULL COMMENT 'Project name',
  `project_id` int(11) DEFAULT NULL COMMENT 'Project id',
  `flow_name` varchar(64) DEFAULT NULL COMMENT 'Flow name',
  `flow_id` int(11) DEFAULT NULL COMMENT 'Flow id',
  `task_name` varchar(64) DEFAULT NULL COMMENT 'Task name',
  `task_id` int(11) DEFAULT NULL COMMENT 'Task id',
  `task_state` varchar(64) DEFAULT NULL COMMENT 'Task state: running, finish',
  `task_instance_id` int(11) NOT NULL COMMENT 'task instance id',
  `execution_time` datetime DEFAULT NULL COMMENT 'Task instance execution time',
  `application_id` varchar(64) DEFAULT NULL COMMENT 'appId',
  `flink_track_url` varchar(255) DEFAULT NULL COMMENT 'flink track url',
  `allocated_mb` int(11) DEFAULT NULL COMMENT 'Total allocated mb acquired by yarn',
  `allocated_vcores` int(11) DEFAULT NULL COMMENT 'The total allocated vcore obtained by yarn',
  `running_containers` int(11) DEFAULT NULL COMMENT 'The total allocated containers obtained by yarn',
  `engine_type` varchar(64) DEFAULT NULL COMMENT 'Engine?',
  `duration` double DEFAULT NULL COMMENT 'Running duration',
  `start_time` datetime DEFAULT NULL COMMENT 'Start time',
  `end_time` datetime DEFAULT NULL COMMENT 'End time',
  `vcore_seconds` float DEFAULT NULL COMMENT 'CPU consuming(vcore-seconds)',
  `memory_seconds` float DEFAULT NULL COMMENT 'Memory consuming(GB-seconds)',
  `queue` varchar(64) DEFAULT NULL COMMENT 'Queue',
  `cluster_name` varchar(64) DEFAULT NULL COMMENT 'Cluster',
  `retry_times` int(11) DEFAULT NULL COMMENT 'Times of retries',
  `execute_user` varchar(64) DEFAULT NULL COMMENT 'Executing user',
  `diagnosis` varchar(255) COMMENT 'Yarn diagnosis',
  `parallel` int(11) DEFAULT NULL COMMENT 'Flink parallel',
  `tm_slot` int(11) DEFAULT NULL COMMENT 'Flink tm slot',
  `tm_core` int(11) DEFAULT NULL COMMENT 'Flink tm core',
  `tm_mem` int(11) DEFAULT NULL COMMENT 'Flink tm_mem',
  `jm_mem` int(11) DEFAULT NULL COMMENT 'Flink jm_mem',
  `job_name` varchar(255) COMMENT 'Job name',
  `create_time` datetime DEFAULT NULL COMMENT 'Create time',
  `update_time` datetime DEFAULT NULL COMMENT 'Update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_application_id` (`application_id`),
  KEY `idx_project_name` (`project_name`),
  KEY `idx_flow_name` (`flow_name`),
  KEY `idx_task_name` (`task_name`),
  KEY `idx_username` (`username`),
  KEY `idx_job_name` (`job_name`),
  KEY `idx_task_state` (`task_state`)
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8 COMMENT='Flink application table';

INSERT INTO `user` (`username`, `password`) values ('compass', 'compass');

-- zh_CN

INSERT INTO `task_diagnosis_advice` VALUES
(1,'driver',NULL,'containerFailed','Container失败','{container}失败，诊断信息为{diagnostics}，退出码为{exitCode}',NULL,'container,diagnostic,exitCode','otherException',0,NULL),
(2,'driver','','stageFailed','Stage失败','Stage{stage}连续{failedNum}次失败，此job彻底失败',NULL,'stage,failedNum','otherException',0,NULL),
(3,'driver','','jobFailedOrAbortedException','任务失败或退出异常','',NULL,NULL,'otherException',0,NULL),
(4,'driver','jobFailedOrAbortedException','fileNotFoundException','写入数据失败，文件不存在','可能存在以下情况：<br/>1、可能存在并发任务操作同一路径导致任务写入失败，建议重试任务<br/>2、可能存在来源表数据被更新，请在来源表更新结束后再重新执行任务<br/>3、表分区存在，但分区路径不存在报错。因为元数据数据还存在，导致元数据去指明路径时路径不存在故报错。建议在删除分区时，用drop分区，这样才会删除数据和分区。',NULL,NULL,'sqlFailed',0,NULL),
(5,'driver','jobFailedOrAbortedException','failedRunJobNoTablePermission','用户没有权限','用户({user})没有对({database}/{table})库表的{option}权限。<a target=\"_blank\" style=\"color: rgb(45, 204, 195);\" href=\"\">请先申请相应权限</a>',NULL,'user,database,table','sqlFailed',0,NULL),
(6,'driver','jobFailedOrAbortedException','sqlSyncError','sql语法错误','外部表必须添加外部地址',NULL,NULL,'sqlFailed',0,NULL),
(7,'driver','jobFailedOrAbortedException','broadcastsTimeout','广播超时','经引擎判断需要广播（自动或者用户强制hint）的表或者子查询的数据需要在{threshold}秒内完成广播至各计算节点',NULL,'threshold','otherException',0,NULL),
(8,'driver','jobFailedOrAbortedException','blockMissingException','块丢失异常','请提供以下报错并联系技术支持。<br/> BlockMissingException: Could not obtain block:xxx',NULL,NULL,'otherException',0,NULL),
(9,'driver','jobFailedOrAbortedException','outOfHeapOOM','shuffle阶段获取数据时堆外内存溢出','建议使用OPPO shuttle服务，开源项目：https://github.com/cubefs/shuttle',NULL,NULL,'memoryOverflow',0,NULL),
(10,'driver',NULL,'shuffleFetchFailed','shuffle连接不上',NULL,NULL,NULL,'shuffleFailed',0,NULL),
(11,'driver',NULL,'otherError','其他错误信息',NULL,NULL,NULL,'otherException',0,NULL),
(12,'executor',NULL,'shuffleBlockFetcherIterator','节点shuffle负载过高','建议使用OPPO shuttle服务，开源项目：https://github.com/cubefs/shuttle',NULL,NULL,'shuffleFailed',0,NULL),
(13,'executor',NULL,'connectorOfExecutorAndDriverException1','executor与driver节点通讯异常',NULL,NULL,NULL,'otherException',0,NULL),
(14,'executor',NULL,'connectorOfExecutorAndDriverException2','executor与driver节点通讯异常',NULL,NULL,NULL,'otherException',0,NULL),
(15,'executor',NULL,'connectorOfExecutorAndDriverException3','executor与driver节点通讯异常',NULL,NULL,NULL,'otherException',0,NULL),
(16,'executor',NULL,'noLzo','机器中无lzo包','请提供任务执行的机器并联系技术支持查看机器lzo包配置。',NULL,NULL,'otherException',0,NULL),
(17,'executor',NULL,'diskBlockObjectWriterError','写入磁盘失败，可能存在磁盘损坏的情况','如重试任务依然失败，且同一个报错，请联系技术支持并提供报错查看问题。',NULL,NULL,'otherException',0,NULL),
(18,'executor',NULL,'transportChannelHandler','请求超时','该类错误未分类',NULL,NULL,'otherException',0,'未检测到异常'),
(19,'executor',NULL,'otherError','其他错误信息',NULL,NULL,NULL,'otherException',0,'未检测到异常'),
(20,'event',NULL,'largeTableScan','大表扫描','表{tables}的扫描量{values}，超过阈值{threshold}行，发生大表扫描。</br>该任务存在情况：<br/>1.大表或全表扫描的情况，大表扫描或者全表扫描耗时长、浪费资源，也会给集群带来一定压力，且容易导致内存溢出任务失败，建议检查执行sql ，确认逻辑，添加分区条件，过滤不需要的数据，避免不必要全表扫描；<br/>2.多次扫描大表的情况，建议将所有的目标数据进行一次大表扫描收集到临时表（或者通过global temp view缓存到内存中，视数据量而写，一般小于几十G的数据量可以缓存），从而将N次扫描大表转换为1次扫描大表+N次扫描小表，减少大消耗的操作。',NULL,'tables,threshold,values','largeTableScan',0,'未检测到异常'),
(21,'event',NULL,'cpuWaste','CPU浪费','app资源总消耗:{appConsume},driver资源浪费:{driverWaste} 占比:{driverWastedPercent},executor资源浪费:{executorWaste} 占比:{executorWastedPercent}。计算资源存在浪费,请适当减小executor的并发数,优化任务。',NULL,' appConsume,driverWastedPercent,executorWastedPercent,executorWaste,driverWaste','cpuWaste',0,'未检测到异常'),
(22,'event',NULL,'dataSkew','数据倾斜',' {dataSkewInfo}，读取的数据量严重超过Stage下的中位值，发生数据倾斜。<br/> 具体解决方法可参考如下建议：<br/>1、找到倾斜阶段对应执行的sql<br/>2、查看关联字段是否有倾斜（join on，group by，partition by 的字段）<br/>3、根据任务需要，如果异常值不需要可以过滤（字段！=异常值)；如果异常值也需要，可以单独处理（第一段sql排除异常值，union all 只有异常值的数据）<br/>',NULL,'dataSkewInfo','dataSkew',0,'未检测到异常'),
(23,'event',NULL,'globalSortAbnormal','全局排序异常','{globalSortInfo}, 发生全局排序异常，请联系技术支持优化代码。',NULL,'globalSortInfo','globalSortAbnormal',0,'未检测到异常'),
(24,'event',NULL,'hdfsStuck','HDFS卡顿','{hdfsSlowInfo}, 发生HDFS卡顿，请及时关注HDFS集群负载情况。',NULL,'hdfsStuckInfo','hdfsStuck',0,'未检测到异常'),
(25,'event',NULL,'jobDurationAbnormal','Job耗时异常','{jobs}空闲时间与总时间的占比为：{values}，超过阈值{threshold}，发生job耗时异常。请及时关注平台运行状态。',NULL,'jobs,values,threshold','jobDurationAbnormal',0,'未检测到异常'),
(26,'event',NULL,'memoryWaste','内存浪费',' Driver分配内存{driverMemory},峰值内存{driverPeak},Executor分配内存{executorMemory},峰值内存{executorPeak}。结合运行时间计算内存浪费为{wasteRatio},超过阈值{threshold}, 建议适当减少driver/executor的内存大小，优化成本',NULL,' driverMemory,driverPeak,executorMemory,executorPeak,threshold,wasteRatio','memoryWaste',0,'未检测到异常'),
(27,'event',NULL,'oom','OOM预警','执行sql中广播内存占比：{scanTable}与{execType}内存占比={usePercent}%<br/>该任务被广播的表与driver或executor任意一个内存占比已超过阈值{oom}%，存在OOM内存溢出风险，建议提前增加相应内存，禁用广播或取消强制广播。<br/>禁用广播参数为：--hiveconf livy.session.conf.spark.sql.autoBroadcastJoinThreshold=-1;<br/>增加内存参数为（增加当前内存的20%）：（executor内存） --hiveconf livy.session.conf.spark.executor.memory;（driver内存）--hiveconf livy.session.conf.spark.driver.memory;<br/>如果非强制广播的情况下，spark2对是否广播判断有一定概率失误，建议切换到spark3。',NULL,'scanTable,execType,usePercent,oom','oomWarn',0,'未检测到异常'),
(28,'event',NULL,'speculativeTask','推测执行过多','{jobStages} 推测执行数量为：{values}，超过阈值{threshold}个，推测执行过多，请联系技术支持优化代码。',NULL,'jobStages,values,threshold','speculativeTask',0,'未检测到异常'),
(29,'event',NULL,'stageDurationAbnormal','Stage耗时异常','{jobStages}空闲时间与该Stage总时间的占比为：{values}，超过阈值{threshold}，该stage耗时异常。请及时关注平台运行状态。',NULL,'jobStages,values,threshold','stageDurationAbnormal',0,'未检测到异常'),
(30,'event',NULL,'taskDurationAbnormal','Task耗时异常','{taskDurationInfo}，任务的运行时间远远大于中位值，发生Task耗时异常。',NULL,'taskDurationInfo,reason','taskDurationAbnormal',0,'未检测到异常'),
(31,'yarn',NULL,'driverOOM','内存溢出','内存溢出导致任务失败，退出码：137。\\n建议调整参数：\\n增加driver内存，增大为现有内存的30%：\\n--conf spark.driver.memory  \\n一般driver内存溢出有两种情况可能发生，一种是读取的文件数量过多，另一种是广播过大数据。第一种情况可以查看是否充分利用分区条件，或者减小数据量，分段执行sql ；第二种情况可以禁用广播，或者取消强制广播。这两种情况都可以通过增加driver内存临时解决。','Application application_.* failed 1 times due to AM Container for appattempt_.*exitCode: 137.*$',NULL,'memoryOverflow',0,'未检测到异常'),
(32,'yarn',NULL,'fileAlreadyExistsException','路径已存在','{filePath}写入路径已存在，建议：可检查代码逻辑，在代码逻辑中添加判断output文件夹是否存在，如果存在则删除。','User class threw exception: org.apache.hadoop.mapred.FileAlreadyExistsException: Output directory (?<filePath>.+) already exists.*$','filePath','otherException',0,'未检测到异常'),
(33,'yarn',NULL,'killByHiveServer','任务异常终止','{job}任务被hiveserver(机器ip:{ip})终止','Kill job (?<job>.+) received from hive.*at (?<ip>.+) Job received Kill while in RUNNING state.*$','job,ip','otherException',0,'未检测到异常'),
(34,'yarn',NULL,'killedByUser',' 任务异常终止','任务被手动终止','Application killed by user.*$',NULL,'otherException',0,'未检测到异常'),
(35,'yarn',NULL,'mysqlPermissionDenied','mysql权限缺失','用户名:{user}@机器ip:{ip}没有mysql的访问权限，请添加相应的mysql权限后，再重试任务。','User class threw exception:.*: Access denied for user \'(?<user>.+)\'@\'(?<ip>.+)\'.*$',' user,ip','sqlFailed',0,'未检测到异常'),
(36,'yarn',NULL,'otherError','AM异常','该类错误未分类','.*',NULL,'otherException',0,'未检测到异常'),
(37,'yarn',NULL,'permissionDenied','权限缺失','用户{user}没有{inode}库表的{access}权限,建议先申请权限再重试。','User class threw exception.*AccessControlException.*user=(?<user>.+), access=(?<access>.+), inode=\\\"(?<inode>.+)\\\".*$','user,access,inode','sqlFailed',0,'未检测到异常'),
(38,'yarn',NULL,'systemTimesError','时间同步异常','服务器时间同步问题，建议任务重试','.*System times on machines may be out of sync.*$',NULL,'otherException',0,'未检测到异常'),
(39,'scheduler',NULL,'otherError','其他错误信息','该类错误未分类',NULL,NULL,'otherException',0,'未检测到异常'),
(40,'event',NULL,'broadcastOOM','广播过滤行数过多','执行sql中广播过滤行数：{maxRows} ,内存占比：{usePercent}%。<br/>该任务广播过滤行数超过{broadcastRows}且该任务被广播的表与driver或executor任意一个内存占比已超过阈值{broadcastRowsOom}%，存在OOM内存溢出风险，建议提前增加相应内存，禁用广播或取消强制广播。<br/>禁用广播参数为：--hiveconf livy.session.conf.spark.sql.autoBroadcastJoinThreshold=-1;<br/>增加内存参数为（增加当前内存的20%）：（executor内存） --hiveconf livy.session.conf.spark.executor.memory;（driver内存）--hiveconf livy.session.conf.spark.driver.memory;<br/>如果非强制广播的情况下，spark2对是否广播判断有一定概率失误，建议切换到spark3。',NULL,'maxRows,usePercent,broadcastRows,broadcastRowsOom','oomWarn',0,'未检测到异常'),
(41,'driver',NULL,'outOfMemoryError','内存溢出','适当增加driver内存',NULL,NULL,'memoryOverflow',0,NULL),
(42, 'mrJobHistory', NULL, 'mrLargeTableScan', 'MR大表扫描', '表的扫描量{values}，超过阈值{threshold}行，发生大表扫描。', NULL, 'threshold,values', 'mrLargeTableScan', 0, '未检测到异常'),
(43, 'mrJobHistory', NULL, 'mrMemoryWaste', 'MR内存浪费', '{memoryWaste}。 建议适当减少内存大小，优化成本', NULL, 'memoryWaste', 'mrMemoryWaste', 0, '未检测到异常'),
(44, 'mrJobHistory', NULL, 'mrDataSkew', 'MR数据倾斜', ' {dataSkewInfo}。{taskType}处理的数据量严重超过中位值，发生数据倾斜。', NULL, 'dataSkewInfo', 'mrDataSkew', 0, '未检测到异常'),
(45, 'mrContainer', NULL, 'otherError', '其他错误信息', '该类错误未分类', NULL, NULL, 'otherException', 0, '未检测到异常'),
(46, 'mrJobHistory', NULL, 'mrSpeculativeTask', 'MR推测执行过多', '推测执行数量为：{values}，其中{attemptId}最大耗时为：{maxElapsedTime}', NULL, 'values,threshold', 'mrSpeculativeTask', 0, '未检测到异常'),
(47, 'mrJobHistory', NULL, 'mrTaskDurationAbnormal', 'MRTask长尾', '{taskDurationInfo}，{taskType}的最大运行耗时远远大于中位值，发生Task耗时异常。', NULL, 'taskDurationInfo', 'mrTaskDurationAbnormal', 0, '未检测到异常'),
(48, 'mrJobHistory', NULL, 'mrGCAbnormal', 'MRGC异常', '{mrGCAbnormal}', NULL, 'mrGCAbnormal', 'mrGCAbnormal', 0, '未检测到异常');

-- en_US

--INSERT INTO `task_diagnosis_advice1` VALUES
--(1,'driver',NULL,'containerFailed','Container failed','{container} failed with diagnostic information {diagnostics}, exit code {exitCode}',NULL,'container,diagnostic,exitCode','otherException',0,NULL),
--(2,'driver','','stageFailed','Stage failed','Stage {stage} has failed {failedNum} consecutive times, job has failed permanently',NULL,'stage,failedNum','otherException',0,NULL),
--(3,'driver','','jobFailedOrAbortedException','Job failed or aborted exception','',NULL,NULL,'otherException',0,NULL),
--(4,'driver','jobFailedOrAbortedException','fileNotFoundException','Failed to write data because file does not exist','Possible scenarios：<br/>1. It may fail to write and update the path of the result table due to concurrent tasks operating on the same path. It is recommended to retry the task.<br/>2. The data of the source table may be updated. Please re-execute the task after the update of the source table has completed.<br/>3. An error occurs in the partition path when the partition exists but the path does not exist. Because the metadata still exists, the metadata specifies that the path does not exist, so an error is reported. It is recommended to use the “drop partition” command to delete the partition, so that both the data and partition will be deleted.',NULL,NULL,'sqlFailed',0,NULL),
--(5,'driver','jobFailedOrAbortedException','failedRunJobNoTablePermission','User does not have permission','User ({user}) does not have {option} permission on ({database}/{table}). <a target=\"_blank\" style=\"color: rgb(45, 204, 195);\" href=\"\">Please apply for the corresponding permission first</a>.',NULL,'user,database,table','sqlFailed',0,NULL),
--(6,'driver','jobFailedOrAbortedException','sqlSyncError','SQL syntax error','External tables must add external address',NULL,NULL,'sqlFailed',0,NULL),
--(7,'driver','jobFailedOrAbortedException','broadcastsTimeout','Broadcast timeout','Tables or subqueries that are determined to need to be broadcasted (automatically or forced by users) need to complete broadcasting to each computing node within {threshold} seconds.',NULL,'threshold','otherException',0,NULL),
--(8,'driver','jobFailedOrAbortedException','blockMissingException','Block missing exception','Please provide the following error message and contact technical support.<br/>BlockMissingException: Could not obtain block:xxx',NULL,NULL,'otherException',0,NULL),
--(9,'driver','jobFailedOrAbortedException','outOfHeapOOM','Out of heap memory when obtaining data during the shuffle process','It is recommended to use OPPO shuttle service, an open source project: https://github.com/cubefs/shuttle',NULL,NULL,'memoryOverflow',0,NULL),
--(10,'driver',NULL,'shuffleFetchFailed','Unable to establish a shuffle connection',NULL,NULL,NULL,'shuffleFailed',0,NULL),
--(11,'driver',NULL,'otherError','Other error message',NULL,NULL,NULL,'otherException',0,NULL),
--(12,'executor',NULL,'shuffleBlockFetcherIterator','Shuffle load imbalance on nodes','It is recommended to use OPPO shuttle service, an open source project: https://github.com/cubefs/shuttle',NULL,NULL,'shuffleFailed',0,NULL),
--(13,'executor',NULL,'connectorOfExecutorAndDriverException1','Communication exception between executor and driver nodes',NULL,NULL,NULL,'otherException',0,NULL),
--(14,'executor',NULL,'connectorOfExecutorAndDriverException2','Communication exception between executor and driver nodes',NULL,NULL,NULL,'otherException',0,NULL),
--(15,'executor',NULL,'connectorOfExecutorAndDriverException3','Communication exception between executor and driver nodes',NULL,NULL,NULL,'otherException',0,NULL),
--(16,'executor',NULL,'noLzo','No LZO package in the machine','Please provide the machine used for task execution and contact technical support to check the LZO package configuration of the machine.',NULL,NULL,'otherException',0,NULL),
--(17,'executor',NULL,'diskBlockObjectWriterError','Failed to write data to disk, possible disk damage','If the task still fails after being retried and the same error occurs, please contact technical support and provide the error message for problem investigation.',NULL,NULL,'otherException',0,NULL),
--(18,'executor',NULL,'transportChannelHandler','Request timeout','This class of errors is uncategorized.',NULL,NULL,'otherException',0,'No anomalies detected'),
--(19,'executor',NULL,'otherError','Other error message','This class of errors is uncategorized.',NULL,NULL,'otherException',0,'No anomalies detected'),
--(20,'event',NULL,'largeTableScan','Large table scan','The scan volume of table {tables} is {values}, which exceeds the threshold of {threshold} rows and triggers a large table scan. <br/> The following conditions may exist in the task: <br/> 1. Large table or full table scan: Large table or full table scans are time-consuming and wasteful of resources, and may cause pressure on the cluster, and is easy to cause task failure due to memory overflow. It is recommended to check the executed SQL statement, confirm the logic, add partitioning conditions, filter unnecessary data, and avoid unnecessary full table scans;<br/> 2. Multiple scans of large tables: It is recommended to collect all target data using one large table scan and store the data in a temporary table (or cache it in memory by using global temp view, depending on the data size. Generally, if the data size is less than a few tens of GB, it can be cached), so that N scans of a large table can be converted into 1 scan of a large table + N scans of small tables, reducing large-scale operations that consume resources.',NULL,'tables,threshold,values','largeTableScan',0,'No anomalies detected'),
--(21,'event',NULL,'cpuWaste','CPU waste','Total resource consumed by app:{appConsume}, Driver resource waste:{driverWaste}, Percentage: {driverWastedPercent}, Executor resource waste:{executorWaste}, Percentage: {executorWastedPercent}. Computing resources are wasted. Please appropriately reduce the concurrency number of Executors and optimize tasks.',NULL,'appConsume,driverWastedPercent,executorWastedPercent,executorWaste,driverWaste','cpuWaste',0,'No anomalies detected'),
--(22,'event',NULL,'dataSkew','Data skew','{dataSkewInfo}, The amount of data read by {tableName} far exceeds the median value of the stage, indicating that skew exists.<br/> Specific solutions can refer to the following suggestions:<br/>1. Find the SQL statement corresponding to the skew stage.<br/>2. Check whether the associated fields are skewed (join on, group by, partition by fields).<br/>3. According to the task needs, if the exception value is not needed, it can be filtered (field! = exception value); if the exception value is also required, it can be processed separately (the first SQL statement excludes the exception value, and union all only includes data with exception values).<br/>',NULL,'dataSkewInfo','dataSkew',0,'No anomalies detected'),
--(23,'event',NULL,'globalSortAbnormal','Global sorting exception','{globalSortInfo}, A global sorting exception occurred. Please contact technical support to optimize the code.',NULL,'globalSortInfo','globalSortAbnormal',0,'No anomalies detected'),
--(24,'event',NULL,'hdfsStuck','HDFS stuck','{hdfsSlowInfo}, HDFS is stuck. Please pay attention to the load situation of the HDFS cluster in time.',NULL,'hdfsStuckInfo','hdfsStuck',0,'No anomalies detected'),
--(25,'event',NULL,'jobDurationAbnormal','Job duration exception','{jobs}, the ratio of idle time and the total time is：{values}, which exceeds the threshold of {threshold}, indicating a job duration exception. Please pay attention to the operation status of the platform in time.',NULL,'jobs,values,threshold','jobDurationAbnormal',0,'No anomalies detected'),
--(26,'event',NULL,'memoryWaste','Memory waste','Driver allocated memory {driverMemory}, peak memory {driverPeak}, Executor allocated memory {executorMemory}, peak memory {executorPeak}. Combined with running time, the memory waste rate is {wasteRatio}, which exceeds the threshold {threshold}. It is recommended to appropriately reduce the memory size of driver/executor and optimize the cost.',NULL,'driverMemory,driverPeak,executorMemory,executorPeak,threshold,wasteRatio','memoryWaste',0,'No anomalies detected'),
--(27,'event',NULL,'oom','OOM warning','The proportion of external table broadcast memory in the SQL statement is {scanTable} and {execType} memory proportion={usePercent}%<br/> The number of broadcast filtering rows of this task exceeds {broadcastRows}, and the internal memory proportion of the table being broadcasted either to driver or executor has exceeded the threshold of {broadcastRowsOom}%, indicating that there is a risk of OOM memory overflow. It is recommended to increase the corresponding memory in advance, disable broadcasting, or cancel forced broadcasting. <br/>The parameter for disabling broadcast is: --hiveconf livy.session.conf.spark.sql.autoBroadcastJoinThreshold=-1;<br/>The parameter for increasing memory (20% of the current memory): executor memory --hiveconf livy.session.conf.spark.executor.memory; driver memory --hiveconf livy.session.conf.spark.driver.memory; <br/>In the case of non-forced broadcast, spark2 has a certain probability of misjudgment on whether to broadcast. It is recommended to switch to spark3.',NULL,'scanTable,execType,usePercent,oom','oomWarn',0,'No anomalies detected'),
--(28,'event',NULL,'speculativeTask','Excessive speculative execution','The number of speculative execution for {jobStages} is {values}, which exceeds the threshold value of {threshold}. There are too many speculative executions. Please contact technical support to optimize the code.',NULL,'jobStages,values,threshold','speculativeTask',0,'No anomalies detected'),
--(29,'event',NULL,'stageDurationAbnormal','Stage duration exception','The proportion of idle time of {jobStages} and the total time is: {values}, which exceeds the threshold of {threshold}, indicating that this stage has a duration exception. Please pay attention to the running status of the platform in time.',NULL,'jobStages,values,threshold','stageDurationAbnormal',0,'No anomalies detected'),
--(30,'event',NULL,'taskDurationAbnormal','Task duration exception','{taskDurationInfo}, the running time of the task is far greater than the median value, indicating a task duration exception.',NULL,'taskDurationInfo,reason','taskDurationAbnormal',0,'No anomalies detected'),
--(31,'yarn',NULL,'driverOOM','Memory overflow','The task failed with exit code 137 due to memory overflow.\\nIt is recommended to adjust the parameters:<br/>Increase driver memory to 30% larger of the current memory: <br/>--conf spark.driver.memory <br/>There are two situations that may cause driver memory overflow. One is that too many files are read, and the other is that too large data is broadcasted. For the first case, you can check whether partition conditions are fully utilized or reduce the amount of data and execute SQL statements in segments; for the second case, you can disable broadcasting or cancel forced broadcasting. These two situations can be temporarily resolved by increasing driver memory. ',NULL,NULL,'memoryOverflow',0,NULL),
--(32,'yarn',NULL,'fileAlreadyExistsException','Path already exists','The write path {filePath} already exists. It is recommended to check the code logic and add a judgment whether the output folder exists in the code logic. If it exists, delete the folder.',NULL,'filePath','otherException',0,NULL),
--(33,'yarn',NULL,'killByHiveServer','Abnormal termination of task','Task {job} is terminated by HiveServer (machine IP: {ip}).', 'Kill job (?<job>.+) received from hive.*at (?<ip>.+) Job received Kill while in RUNNING state.*$',NULL,'otherException',0,NULL),
--(34,'yarn',NULL,'killedByUser','Abnormal termination of task','The task is terminated manually.','Application killed by user.*$',NULL,'otherException',0,NULL),
--(35,'yarn',NULL,'mysqlPermissionDenied','MySQL permission missing','User name: {user}@machine IP: {ip} does not have access to MySQL, please add the corresponding MySQL permission and then retry the task.', 'User class threw exception:.*: Access denied for user \'(?<user>.+)\'@\'(?<ip>.+)\'.*$', 'user,ip','sqlFailed',0,NULL),
--(36,'yarn',NULL,'otherError','AM exception','This type of error is uncategorized.','.*',NULL,'otherException',0,'No exception detected'),
--(37,'yarn',NULL,'permissionDenied','Lack of permissions','User {user} does not have {access} access to table {inode}, it is recommended to apply for permissions before retrying.','User class threw exception.*AccessControlException.*user=(?<user>.+), access=(?<access>.+), inode=\\\"(?<inode>.+)\\\".*$','user,access,inode','sqlFailed',0,'No exception detected'),
--(38,'yarn',NULL,'systemTimesError','Time synchronization exception','Server time synchronization problem, it is recommended to retry the task','.*System times on machines may be out of sync.*$',NULL,'otherException',0,'No exception detected'),
--(39,'scheduler',NULL,'otherError','Other error information','This type of error is uncategorized',NULL,NULL,'otherException',0,'No exception detected'),
--(40,'event',NULL,'broadcastOOM','Broadcast filtering exceeds limit','Broadcast filter rows in SQL: {maxRows}, memory occupancy:{usePercent}%.<br/>This task has broadcasted rows greater than {broadcastRows} and the memory occupancy of the table being broadcasted by the driver or executor has exceeded the threshold of {broadcastRowsOom}%, there is a risk of OOM memory overflow. It is recommended to increase the corresponding memory in advance, disable broadcasting or cancel forced broadcasting. <br/>Disable broadcasting parameter is: --hiveconf livy.session.conf.spark.sql.autoBroadcastJoinThreshold=-1; <br/>Add memory parameter (increase current memory by 20%): (executor memory)--hiveconf livy.session.conf.spark.executor.memory; (driver memory) --hiveconf livy.session.conf.spark.driver.memory;<br/>If there is not force broadcasting, there is a certain probability for spark2 to misjudge whether to broadcast or not. It is recommended to switch to spark3.',NULL,'maxRows,usePercent,broadcastRows,broadcastRowsOom','oomWarn',0,'No exception detected'),
--(41,'driver',NULL,'outOfMemoryError','Memory overflow','Increase driver memory properly',NULL,NULL,'memoryOverflow',0,NULL),
--(42, 'mrJobHistory', NULL, 'mrLargeTableScan', 'MR big table scanning', 'Scanning amount of table {values} exceeded the threshold of {threshold} rows, causing big table scanning.', NULL, 'threshold,values', 'mrLargeTableScan', 0, 'No exception detected'),
--(43, 'mrJobHistory', NULL, 'mrMemoryWaste', 'MR memory waste', '{memoryWaste}. It is recommended to reduce the memory size appropriately and optimize costs.', NULL, 'memoryWaste', 'mrMemoryWaste', 0, 'No exception detected'),
--(44, 'mrJobHistory', NULL, 'mrDataSkew', 'MR data skewness', ' {dataSkewInfo}. The amount of data processed by {taskType} exceeds the median value, resulting in data skewness.', NULL, 'dataSkewInfo', 'mrDataSkew', 0, 'No exception detected'),
--(45, 'mrContainer', NULL, 'otherError', 'Other error information', 'This type of error is uncategorized', NULL, NULL, 'otherException', 0, 'No exception detected'),
--(46, 'mrJobHistory', NULL, 'mrSpeculativeTask', 'Too many MR speculative executions', 'The number of speculative executions is: {values}, among which {attemptId} has the longest elapsed time: {maxElapsedTime}.', NULL, 'values,threshold', 'mrSpeculativeTask', 0, 'No exception detected'),
--(47, 'mrJobHistory', NULL, 'mrTaskDurationAbnormal', 'MR task with long tail', '{taskDurationInfo}, the maximum running time of {taskType} is much longer than the median value, resulting in Task time abnormality.', NULL, 'taskDurationInfo', 'mrTaskDurationAbnormal', 0, 'No exception detected'),
--(48, 'mrJobHistory', NULL, 'mrGCAbnormal', 'MR GC abnormality', '{mrGCAbnormal}', NULL, 'mrGCAbnormal', 'mrGCAbnormal', 0, 'No exception detected');
