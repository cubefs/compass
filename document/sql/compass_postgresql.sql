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

CREATE TABLE user_info (
    id serial PRIMARY KEY NOT NULL,
    user_id bigint DEFAULT '0'::bigint NOT NULL,
    username character varying(64),
    password character varying(256),
    is_admin integer DEFAULT 0,
    icon character varying(500),
    email character varying(100),
    phone character varying(64),
    create_time timestamp DEFAULT NULL,
    update_time timestamp DEFAULT NULL,
    login_time timestamp DEFAULT NULL,
    status integer DEFAULT 1,
    scheduler_type character varying(64)
);
CREATE UNIQUE INDEX idx_user_username ON user_info (username, scheduler_type);


CREATE TABLE project (
    id serial PRIMARY KEY NOT NULL,
    project_name character varying(64),
    description character varying(2048),
    user_id bigint,
    project_status integer DEFAULT 1,
    create_time timestamp DEFAULT NULL,
    update_time timestamp DEFAULT NULL
);
CREATE INDEX idx_project ON project (project_name);
CREATE INDEX idx_user_id ON project (user_id);

CREATE TABLE flow (
    id serial PRIMARY KEY NOT NULL,
    flow_name character varying(64),
    description character varying(2048),
    user_id bigint,
    flow_status integer DEFAULT 1,
    project_id bigint,
    project_name character varying(64),
    create_time timestamp DEFAULT NULL,
    update_time timestamp DEFAULT NULL
);
CREATE INDEX idx_flow ON flow (flow_name);
CREATE INDEX idx_user_id ON flow (user_id);

CREATE TABLE task (
    id serial PRIMARY KEY NOT NULL,
    project_name character varying(64),
    project_id bigint,
    flow_name character varying(64),
    flow_id bigint,
    task_name character varying(64),
    description character varying(2048),
    user_id bigint,
    task_type character varying(50),
    retries bigint,
    create_time timestamp DEFAULT NULL,
    update_time timestamp DEFAULT NULL
);

CREATE TABLE task_instance (
    id serial PRIMARY KEY NOT NULL,
    project_name character varying(64),
    flow_name character varying(64),
    task_name character varying(64),
    execution_time timestamp DEFAULT NULL,
    start_time timestamp DEFAULT NULL,
    end_time timestamp DEFAULT NULL,
    task_state character varying(64),
    task_type character varying(64),
    retry_times bigint,
    max_retry_times integer,
    worker_group character varying(64),
    trigger_type character varying(32),
    create_time timestamp DEFAULT NULL,
    update_time timestamp DEFAULT NULL
);
CREATE UNIQUE INDEX idx_project_flow_task_execution_date_retry ON task_instance (project_name, flow_name, task_name, execution_time, retry_times);
CREATE INDEX idx_flow_task_execution ON task_instance (flow_name, task_name, execution_time);

CREATE TABLE task_application (
    id serial PRIMARY KEY NOT NULL,
    application_id character varying(64),
    task_name character varying(64),
    flow_name character varying(64),
    project_name character varying(64),
    task_type character varying(32),
    execute_time timestamp DEFAULT NULL,
    retry_times integer,
    log_path text,
    create_time timestamp DEFAULT NULL,
    update_time timestamp DEFAULT NULL
);
CREATE UNIQUE INDEX idx_project_flow_task_appid ON task_application (project_name, flow_name, task_name, execute_time, application_id);

CREATE TABLE blocklist (
    id serial PRIMARY KEY NOT NULL,
    component character varying(64),
    project_name character varying(64),
    flow_name character varying(64),
    task_name character varying(64),
    username character varying(64),
    create_time timestamp DEFAULT NULL,
    update_time timestamp DEFAULT NULL,
    deleted bigint DEFAULT '0'::bigint NOT NULL
);
CREATE UNIQUE INDEX idx_project_flow_task_component ON blocklist (project_name, flow_name, task_name, component);

CREATE TABLE task_syncer_init (
    is_init bigint DEFAULT '0'::bigint NOT NULL
);
CREATE UNIQUE INDEX idx_is_init ON task_syncer_init (is_init);

CREATE TABLE task_diagnosis_advice (
    id serial PRIMARY KEY NOT NULL,
    log_type character varying(64),
    parent_action character varying(64),
    action character varying(64),
    description character varying(255),
    abnormal_advice text,
    rule text,
    variables character varying(255),
    category character varying(255),
    deleted bigint DEFAULT '0'::bigint,
    normal_advice character varying(255)
);
CREATE UNIQUE INDEX idx_logtype_action ON task_diagnosis_advice (log_type, action);

CREATE TABLE task_datum (
    id serial PRIMARY KEY NOT NULL,
    project_name character varying(64),
    flow_name character varying(180),
    task_name character varying(180),
    execution_date timestamp DEFAULT NULL,
    baseline text,
    create_time timestamp DEFAULT NULL
);
CREATE INDEX idx_projectname_flowname_taskname_executiondate ON task_datum USING btree (project_name, flow_name, task_name, execution_date);

CREATE TABLE flink_task (
    id serial PRIMARY KEY NOT NULL,
    username character varying(64),
    user_id bigint,
    project_name character varying(64),
    project_id bigint,
    flow_name character varying(64),
    flow_id bigint,
    task_name character varying(64),
    task_id bigint,
    deleted bigint DEFAULT '0'::bigint NOT NULL,
    create_time timestamp DEFAULT NULL,
    update_time timestamp DEFAULT NULL
);
CREATE UNIQUE INDEX idx_project_flow_task ON flink_task USING btree (project_name, flow_name, task_name);

CREATE TABLE flink_task_app (
    id serial PRIMARY KEY NOT NULL,
    username character varying(64),
    user_id bigint,
    project_name character varying(64),
    project_id bigint,
    flow_name character varying(64),
    flow_id bigint,
    task_name character varying(64),
    task_id bigint,
    task_state character varying(64),
    task_instance_id bigint NOT NULL,
    execution_time timestamp DEFAULT NULL,
    application_id character varying(64),
    flink_track_url character varying(255),
    allocated_mb bigint,
    allocated_vcores bigint,
    running_containers bigint,
    engine_type character varying(64),
    duration double precision,
    start_time timestamp DEFAULT NULL,
    end_time timestamp DEFAULT NULL,
    vcore_seconds double precision,
    memory_seconds double precision,
    queue character varying(64),
    cluster_name character varying(64),
    retry_times bigint,
    execute_user character varying(64),
    diagnosis character varying(255),
    parallel bigint,
    tm_slot bigint,
    tm_core bigint,
    tm_mem bigint,
    jm_mem bigint,
    job_name character varying(255),
    create_time timestamp DEFAULT NULL,
    update_time timestamp DEFAULT NULL
);
CREATE UNIQUE INDEX idx_application_id ON flink_task_app (application_id);
CREATE INDEX idx_project_name ON flink_task_app (project_name);
CREATE INDEX idx_flow_name ON flink_task_app (flow_name);
CREATE INDEX idx_task_name ON flink_task_app (task_name);
CREATE INDEX idx_username ON flink_task_app (username);
CREATE INDEX idx_job_name ON flink_task_app (job_name);
CREATE INDEX idx_task_state ON flink_task_app (task_state);

CREATE TABLE template (
    id serial PRIMARY KEY NOT NULL,
    cid character varying(64),
    cluster text,
    advice text,
    raw_log text,
    create_time timestamp DEFAULT NULL,
    update_time timestamp DEFAULT NULL
);
CREATE UNIQUE INDEX idx_cid ON template (cid);

INSERT INTO user_info (username, password) values ('compass', 'compass');

INSERT INTO task_diagnosis_advice (id, log_type, parent_action, action, description, abnormal_advice, rule, variables, category, deleted, normal_advice) VALUES (1, 'driver', NULL, 'containerFailed', 'Container失败', '{container}失败，诊断信息为{diagnostics}，退出码为{exitCode}', NULL, 'container,diagnostic,exitCode', 'otherException', 0, NULL);
INSERT INTO task_diagnosis_advice (id, log_type, parent_action, action, description, abnormal_advice, rule, variables, category, deleted, normal_advice) VALUES (2, 'driver', '', 'stageFailed', 'Stage失败', 'Stage{stage}连续{failedNum}次失败，此job彻底失败', NULL, 'stage,failedNum', 'otherException', 0, NULL);
INSERT INTO task_diagnosis_advice (id, log_type, parent_action, action, description, abnormal_advice, rule, variables, category, deleted, normal_advice) VALUES (3, 'driver', '', 'jobFailedOrAbortedException', '任务失败或退出异常', '', NULL, NULL, 'otherException', 0, NULL);
INSERT INTO task_diagnosis_advice (id, log_type, parent_action, action, description, abnormal_advice, rule, variables, category, deleted, normal_advice) VALUES (4, 'driver', 'jobFailedOrAbortedException', 'fileNotFoundException', '写入数据失败，文件不存在', '可能存在以下情况：<br/>1、可能存在并发任务操作同一路径导致任务写入失败，建议重试任务<br/>2、可能存在来源表数据被更新，请在来源表更新结束后再重新执行任务<br/>3、表分区存在，但分区路径不存在报错。因为元数据数据还存在，导致元数据去指明路径时路径不存在故报错。建议在删除分区时，用drop分区，这样才会删除数据和分区。', NULL, NULL, 'sqlFailed', 0, NULL);
INSERT INTO task_diagnosis_advice (id, log_type, parent_action, action, description, abnormal_advice, rule, variables, category, deleted, normal_advice) VALUES (5, 'driver', 'jobFailedOrAbortedException', 'failedRunJobNoTablePermission', '用户没有权限', '用户({user})没有对({database}/{table})库表的{option}权限。<a target="_blank" style="color: rgb(45, 204, 195);" href="">请先申请相应权限</a>', NULL, 'user,database,table', 'sqlFailed', 0, NULL);
INSERT INTO task_diagnosis_advice (id, log_type, parent_action, action, description, abnormal_advice, rule, variables, category, deleted, normal_advice) VALUES (6, 'driver', 'jobFailedOrAbortedException', 'sqlSyncError', 'sql语法错误', '外部表必须添加外部地址', NULL, NULL, 'sqlFailed', 0, NULL);
INSERT INTO task_diagnosis_advice (id, log_type, parent_action, action, description, abnormal_advice, rule, variables, category, deleted, normal_advice) VALUES (7, 'driver', 'jobFailedOrAbortedException', 'broadcastsTimeout', '广播超时', '经引擎判断需要广播（自动或者用户强制hint）的表或者子查询的数据需要在600秒(默认)内完成广播至各计算节点', NULL, NULL, 'otherException', 0, NULL);
INSERT INTO task_diagnosis_advice (id, log_type, parent_action, action, description, abnormal_advice, rule, variables, category, deleted, normal_advice) VALUES (8, 'driver', 'jobFailedOrAbortedException', 'blockMissingException', '块丢失异常', '请提供以下报错并联系技术支持。<br/> BlockMissingException: Could not obtain block:xxx', NULL, NULL, 'otherException', 0, NULL);
INSERT INTO task_diagnosis_advice (id, log_type, parent_action, action, description, abnormal_advice, rule, variables, category, deleted, normal_advice) VALUES (9, 'driver', 'jobFailedOrAbortedException', 'outOfHeapOOM', 'shuffle阶段获取数据时堆外内存溢出', '建议使用OPPO shuttle服务，开源项目：https://github.com/cubefs/shuttle', NULL, NULL, 'memoryOverflow', 0, NULL);
INSERT INTO task_diagnosis_advice (id, log_type, parent_action, action, description, abnormal_advice, rule, variables, category, deleted, normal_advice) VALUES (10, 'driver', NULL, 'shuffleFetchFailed', 'shuffle连接不上', NULL, NULL, NULL, 'shuffleFailed', 0, NULL);
INSERT INTO task_diagnosis_advice (id, log_type, parent_action, action, description, abnormal_advice, rule, variables, category, deleted, normal_advice) VALUES (11, 'driver', NULL, 'otherError', '其他错误信息', NULL, NULL, NULL, 'otherException', 0, NULL);
INSERT INTO task_diagnosis_advice (id, log_type, parent_action, action, description, abnormal_advice, rule, variables, category, deleted, normal_advice) VALUES (12, 'executor', NULL, 'shuffleBlockFetcherIterator', '节点shuffle负载过高', '建议使用OPPO shuttle服务，开源项目：https://github.com/cubefs/shuttle', NULL, NULL, 'shuffleFailed', 0, NULL);
INSERT INTO task_diagnosis_advice (id, log_type, parent_action, action, description, abnormal_advice, rule, variables, category, deleted, normal_advice) VALUES (13, 'executor', NULL, 'connectorOfExecutorAndDriverException1', 'executor与driver节点通讯异常', NULL, NULL, NULL, 'otherException', 0, NULL);
INSERT INTO task_diagnosis_advice (id, log_type, parent_action, action, description, abnormal_advice, rule, variables, category, deleted, normal_advice) VALUES (14, 'executor', NULL, 'connectorOfExecutorAndDriverException2', 'executor与driver节点通讯异常', NULL, NULL, NULL, 'otherException', 0, NULL);
INSERT INTO task_diagnosis_advice (id, log_type, parent_action, action, description, abnormal_advice, rule, variables, category, deleted, normal_advice) VALUES (15, 'executor', NULL, 'connectorOfExecutorAndDriverException3', 'executor与driver节点通讯异常', NULL, NULL, NULL, 'otherException', 0, NULL);
INSERT INTO task_diagnosis_advice (id, log_type, parent_action, action, description, abnormal_advice, rule, variables, category, deleted, normal_advice) VALUES (16, 'executor', NULL, 'noLzo', '机器中无lzo包', '请提供任务执行的机器并联系技术支持查看机器lzo包配置。', NULL, NULL, 'otherException', 0, NULL);
INSERT INTO task_diagnosis_advice (id, log_type, parent_action, action, description, abnormal_advice, rule, variables, category, deleted, normal_advice) VALUES (17, 'executor', NULL, 'diskBlockObjectWriterError', '写入磁盘失败，可能存在磁盘损坏的情况', '如重试任务依然失败，且同一个报错，请联系技术支持并提供报错查看问题。', NULL, NULL, 'otherException', 0, NULL);
INSERT INTO task_diagnosis_advice (id, log_type, parent_action, action, description, abnormal_advice, rule, variables, category, deleted, normal_advice) VALUES (18, 'executor', NULL, 'transportChannelHandler', '请求超时', '该类错误未分类', NULL, NULL, 'otherException', 0, '未检测到异常');
INSERT INTO task_diagnosis_advice (id, log_type, parent_action, action, description, abnormal_advice, rule, variables, category, deleted, normal_advice) VALUES (19, 'executor', NULL, 'otherError', '其他错误信息', NULL, NULL, NULL, 'otherException', 0, '未检测到异常');
INSERT INTO task_diagnosis_advice (id, log_type, parent_action, action, description, abnormal_advice, rule, variables, category, deleted, normal_advice) VALUES (20, 'event', NULL, 'largeTableScan', '大表扫描', '表{tables}的扫描量{values}，超过阈值{threshold}行，发生大表扫描。</br>该任务存在情况：<br/>1.大表或全表扫描的情况，大表扫描或者全表扫描耗时长、浪费资源，也会给集群带来一定压力，且容易导致内存溢出任务失败，建议检查执行sql ，确认逻辑，添加分区条件，过滤不需要的数据，避免不必要全表扫描；<br/>2.多次扫描大表的情况，建议将所有的目标数据进行一次大表扫描收集到临时表（或者通过global temp view缓存到内存中，视数据量而写，一般小于几十G的数据量可以缓存），从而将N次扫描大表转换为1次扫描大表+N次扫描小表，减少大消耗的操作。', NULL, 'tables,threshold,values', 'largeTableScan', 0, '未检测到异常');
INSERT INTO task_diagnosis_advice (id, log_type, parent_action, action, description, abnormal_advice, rule, variables, category, deleted, normal_advice) VALUES (21, 'event', NULL, 'cpuWaste', 'CPU浪费', 'app资源总消耗:{appConsume},driver资源浪费:{driverWaste} 占比:{driverWastedPercent},executor资源浪费:{executorWaste} 占比:{executorWastedPercent}。计算资源存在浪费,请适当减小executor的并发数,优化任务。', NULL, ' appConsume,driverWastedPercent,executorWastedPercent,executorWaste,driverWaste', 'cpuWaste', 0, '未检测到异常');
INSERT INTO task_diagnosis_advice (id, log_type, parent_action, action, description, abnormal_advice, rule, variables, category, deleted, normal_advice) VALUES (22, 'event', NULL, 'dataSkew', '数据倾斜', ' {dataSkewInfo}，读取的数据量严重超过Stage下的中位值，发生数据倾斜。<br/> 具体解决方法可参考如下建议：<br/>1、找到倾斜阶段对应执行的sql<br/>2、查看关联字段是否有倾斜（join on，group by，partition by 的字段）<br/>3、根据任务需要，如果异常值不需要可以过滤（字段！=异常值)；如果异常值也需要，可以单独处理（第一段sql排除异常值，union all 只有异常值的数据）<br/>', NULL, 'dataSkewInfo', 'dataSkew', 0, '未检测到异常');
INSERT INTO task_diagnosis_advice (id, log_type, parent_action, action, description, abnormal_advice, rule, variables, category, deleted, normal_advice) VALUES (23, 'event', NULL, 'globalSortAbnormal', '全局排序异常', '{globalSortInfo}, 发生全局排序异常，请联系技术支持优化代码。', NULL, 'globalSortInfo', 'globalSortAbnormal', 0, '未检测到异常');
INSERT INTO task_diagnosis_advice (id, log_type, parent_action, action, description, abnormal_advice, rule, variables, category, deleted, normal_advice) VALUES (24, 'event', NULL, 'hdfsStuck', 'HDFS卡顿', '{hdfsSlowInfo}, 发生HDFS卡顿，请及时关注HDFS集群负载情况。', NULL, 'hdfsStuckInfo', 'hdfsStuck', 0, '未检测到异常');
INSERT INTO task_diagnosis_advice (id, log_type, parent_action, action, description, abnormal_advice, rule, variables, category, deleted, normal_advice) VALUES (25, 'event', NULL, 'jobDurationAbnormal', 'Job耗时异常', '{jobs}空闲时间与总时间的占比为：{values}，超过阈值{threshold}，发生job耗时异常。请及时关注平台运行状态。', NULL, 'jobs,values,threshold', 'jobDurationAbnormal', 0, '未检测到异常');
INSERT INTO task_diagnosis_advice (id, log_type, parent_action, action, description, abnormal_advice, rule, variables, category, deleted, normal_advice) VALUES (26, 'event', NULL, 'memoryWaste', '内存浪费', ' Driver分配内存{driverMemory},峰值内存{driverPeak},Executor分配内存{executorMemory},峰值内存{executorPeak}。结合运行时间计算内存浪费为{wasteRatio},超过阈值{threshold}, 建议适当减少driver/executor的内存大小，优化成本', NULL, ' driverMemory,driverPeak,executorMemory,executorPeak,threshold,wasteRatio', 'memoryWaste', 0, '未检测到异常');
INSERT INTO task_diagnosis_advice (id, log_type, parent_action, action, description, abnormal_advice, rule, variables, category, deleted, normal_advice) VALUES (27, 'event', NULL, 'oom', 'OOM预警', '执行sql中广播内存占比：{scanTable}与{execType}内存占比={usePercent}%<br/>该任务被广播的表与driver或executor任意一个内存占比已超过阈值{oom}%，存在OOM内存溢出风险，建议提前增加相应内存，禁用广播或取消强制广播。<br/>禁用广播参数为：--hiveconf livy.session.conf.spark.sql.autoBroadcastJoinThreshold=-1;<br/>增加内存参数为（增加当前内存的20%）：（executor内存） --hiveconf livy.session.conf.spark.executor.memory;（driver内存）--hiveconf livy.session.conf.spark.driver.memory;<br/>如果非强制广播的情况下，spark2对是否广播判断有一定概率失误，建议切换到spark3。', NULL, 'scanTable,execType,usePercent,oom', 'oomWarn', 0, '未检测到异常');
INSERT INTO task_diagnosis_advice (id, log_type, parent_action, action, description, abnormal_advice, rule, variables, category, deleted, normal_advice) VALUES (28, 'event', NULL, 'speculativeTask', '推测执行过多', '{jobStages} 推测执行数量为：{values}，超过阈值{threshold}个，推测执行过多，请联系技术支持优化代码。', NULL, 'jobStages,values,threshold', 'speculativeTask', 0, '未检测到异常');
INSERT INTO task_diagnosis_advice (id, log_type, parent_action, action, description, abnormal_advice, rule, variables, category, deleted, normal_advice) VALUES (29, 'event', NULL, 'stageDurationAbnormal', 'Stage耗时异常', '{jobStages}空闲时间与该Stage总时间的占比为：{values}，超过阈值{threshold}，该stage耗时异常。请及时关注平台运行状态。', NULL, 'jobStages,values,threshold', 'stageDurationAbnormal', 0, '未检测到异常');
INSERT INTO task_diagnosis_advice (id, log_type, parent_action, action, description, abnormal_advice, rule, variables, category, deleted, normal_advice) VALUES (30, 'event', NULL, 'taskDurationAbnormal', 'Task耗时异常', '{taskDurationInfo}，任务的运行时间远远大于中位值，发生Task耗时异常。', NULL, 'taskDurationInfo,reason', 'taskDurationAbnormal', 0, '未检测到异常');
INSERT INTO task_diagnosis_advice (id, log_type, parent_action, action, description, abnormal_advice, rule, variables, category, deleted, normal_advice) VALUES (31, 'yarn', NULL, 'driverOOM', '内存溢出', '内存溢出导致任务失败，退出码：137。\n建议调整参数：\n增加driver内存，增大为现有内存的30%：\n--conf spark.driver.memory  \n一般driver内存溢出有两种情况可能发生，一种是读取的文件数量过多，另一种是广播过大数据。第一种情况可以查看是否充分利用分区条件，或者减小数据量，分段执行sql ；第二种情况可以禁用广播，或者取消强制广播。这两种情况都可以通过增加driver内存临时解决。', 'Application application_.* failed 1 times due to AM Container for appattempt_.*exitCode: 137.*$', NULL, 'memoryOverflow', 0, '未检测到异常');
INSERT INTO task_diagnosis_advice (id, log_type, parent_action, action, description, abnormal_advice, rule, variables, category, deleted, normal_advice) VALUES (32, 'yarn', NULL, 'fileAlreadyExistsException', '路径已存在', '{filePath}写入路径已存在，建议：可检查代码逻辑，在代码逻辑中添加判断output文件夹是否存在，如果存在则删除。', 'User class threw exception: org.apache.hadoop.mapred.FileAlreadyExistsException: Output directory (?<filePath>.+) already exists.*$', 'filePath', 'otherException', 0, '未检测到异常');
INSERT INTO task_diagnosis_advice (id, log_type, parent_action, action, description, abnormal_advice, rule, variables, category, deleted, normal_advice) VALUES (33, 'yarn', NULL, 'killByHiveServer', '任务异常终止', '{job}任务被hiveserver(机器ip:{ip})终止', 'Kill job (?<job>.+) received from hive.*at (?<ip>.+) Job received Kill while in RUNNING state.*$', 'job,ip', 'otherException', 0, '未检测到异常');
INSERT INTO task_diagnosis_advice (id, log_type, parent_action, action, description, abnormal_advice, rule, variables, category, deleted, normal_advice) VALUES (34, 'yarn', NULL, 'killedByUser', ' 任务异常终止', '任务被手动终止', 'Application killed by user.*$', NULL, 'otherException', 0, '未检测到异常');
INSERT INTO task_diagnosis_advice (id, log_type, parent_action, action, description, abnormal_advice, rule, variables, category, deleted, normal_advice) VALUES (35, 'yarn', NULL, 'mysqlPermissionDenied', 'mysql权限缺失', '用户名:{user}@机器ip:{ip}没有mysql的访问权限，请添加相应的mysql权限后，再重试任务。', 'User class threw exception:.*: Access denied for user ''(?<user>.+)''@''(?<ip>.+)''.*$', ' user,ip', 'sqlFailed', 0, '未检测到异常');
INSERT INTO task_diagnosis_advice (id, log_type, parent_action, action, description, abnormal_advice, rule, variables, category, deleted, normal_advice) VALUES (36, 'yarn', NULL, 'otherError', 'AM异常', '该类错误未分类', '.*', NULL, 'otherException', 0, '未检测到异常');
INSERT INTO task_diagnosis_advice (id, log_type, parent_action, action, description, abnormal_advice, rule, variables, category, deleted, normal_advice) VALUES (37, 'yarn', NULL, 'permissionDenied', '权限缺失', '用户{user}没有{inode}库表的{access}权限,建议先申请权限再重试。', 'User class threw exception.*AccessControlException.*user=(?<user>.+), access=(?<access>.+), inode=\"(?<inode>.+)\".*$', 'user,access,inode', 'sqlFailed', 0, '未检测到异常');
INSERT INTO task_diagnosis_advice (id, log_type, parent_action, action, description, abnormal_advice, rule, variables, category, deleted, normal_advice) VALUES (38, 'yarn', NULL, 'systemTimesError', '时间同步异常', '服务器时间同步问题，建议任务重试', '.*System times on machines may be out of sync.*$', NULL, 'otherException', 0, '未检测到异常');
INSERT INTO task_diagnosis_advice (id, log_type, parent_action, action, description, abnormal_advice, rule, variables, category, deleted, normal_advice) VALUES (39, 'scheduler', NULL, 'otherError', '其他错误信息', '该类错误未分类', NULL, NULL, 'otherException', 0, '未检测到异常');
INSERT INTO task_diagnosis_advice (id, log_type, parent_action, action, description, abnormal_advice, rule, variables, category, deleted, normal_advice) VALUES (40, 'event', NULL, 'broadcastOOM', '广播过滤行数过多', '执行sql中广播过滤行数：{maxRows} ,内存占比：{usePercent}%。<br/>该任务广播过滤行数超过{broadcastRows}且该任务被广播的表与driver或executor任意一个内存占比已超过阈值{broadcastRowsOom}%，存在OOM内存溢出风险，建议提前增加相应内存，禁用广播或取消强制广播。<br/>禁用广播参数为：--hiveconf livy.session.conf.spark.sql.autoBroadcastJoinThreshold=-1;<br/>增加内存参数为（增加当前内存的20%）：（executor内存） --hiveconf livy.session.conf.spark.executor.memory;（driver内存）--hiveconf livy.session.conf.spark.driver.memory;<br/>如果非强制广播的情况下，spark2对是否广播判断有一定概率失误，建议切换到spark3。', NULL, 'maxRows,usePercent,broadcastRows,broadcastRowsOom', 'oomWarn', 0, '未检测到异常');
INSERT INTO task_diagnosis_advice (id, log_type, parent_action, action, description, abnormal_advice, rule, variables, category, deleted, normal_advice) VALUES (41, 'driver', NULL, 'outOfMemoryError', '内存溢出', '适当增加driver内存', NULL, NULL, 'memoryOverflow', 0, NULL);
INSERT INTO task_diagnosis_advice (id, log_type, parent_action, action, description, abnormal_advice, rule, variables, category, deleted, normal_advice) VALUES (42, 'mrJobHistory', NULL, 'mrLargeTableScan', 'MR大表扫描', '表的扫描量{values}，超过阈值{threshold}行，发生大表扫描。', NULL, 'threshold,values', 'mrLargeTableScan', 0, '未检测到异常');
INSERT INTO task_diagnosis_advice (id, log_type, parent_action, action, description, abnormal_advice, rule, variables, category, deleted, normal_advice) VALUES (43, 'mrJobHistory', NULL, 'mrMemoryWaste', 'MR内存浪费', '{memoryWaste}。 建议适当减少内存大小，优化成本', NULL, 'memoryWaste', 'mrMemoryWaste', 0, '未检测到异常');
INSERT INTO task_diagnosis_advice (id, log_type, parent_action, action, description, abnormal_advice, rule, variables, category, deleted, normal_advice) VALUES (44, 'mrJobHistory', NULL, 'mrDataSkew', 'MR数据倾斜', ' {dataSkewInfo}。{taskType}处理的数据量严重超过中位值，发生数据倾斜。', NULL, 'dataSkewInfo', 'mrDataSkew', 0, '未检测到异常');
INSERT INTO task_diagnosis_advice (id, log_type, parent_action, action, description, abnormal_advice, rule, variables, category, deleted, normal_advice) VALUES (45, 'mrContainer', NULL, 'otherError', '其他错误信息', '该类错误未分类', NULL, NULL, 'otherException', 0, '未检测到异常');
INSERT INTO task_diagnosis_advice (id, log_type, parent_action, action, description, abnormal_advice, rule, variables, category, deleted, normal_advice) VALUES (46, 'mrJobHistory', NULL, 'mrSpeculativeTask', 'MR推测执行过多', '推测执行数量为：{values}，其中{attemptId}最大耗时为：{maxElapsedTime}', NULL, 'values,threshold', 'mrSpeculativeTask', 0, '未检测到异常');
INSERT INTO task_diagnosis_advice (id, log_type, parent_action, action, description, abnormal_advice, rule, variables, category, deleted, normal_advice) VALUES (47, 'mrJobHistory', NULL, 'mrTaskDurationAbnormal', 'MRTask长尾', '{taskDurationInfo}，{taskType}的最大运行耗时远远大于中位值，发生Task耗时异常。', NULL, 'taskDurationInfo', 'mrTaskDurationAbnormal', 0, '未检测到异常');
INSERT INTO task_diagnosis_advice (id, log_type, parent_action, action, description, abnormal_advice, rule, variables, category, deleted, normal_advice) VALUES (48, 'mrJobHistory', NULL, 'mrGCAbnormal', 'MRGC异常', '{mrGCAbnormal}', NULL, 'mrGCAbnormal', 'mrGCAbnormal', 0, '未检测到异常');
