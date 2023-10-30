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

--
-- Table structure for table t_ds_process_definition
--

DROP TABLE IF EXISTS t_ds_process_definition;
CREATE TABLE t_ds_process_definition (
  id serial NOT NULL  ,
  code bigint NOT NULL,
  name varchar(255) DEFAULT NULL ,
  version int NOT NULL ,
  description text ,
  project_code bigint DEFAULT NULL ,
  release_state int DEFAULT NULL ,
  user_id int DEFAULT NULL ,
  global_params text ,
  locations text ,
  warning_group_id int DEFAULT NULL ,
  flag int DEFAULT NULL ,
  timeout int DEFAULT '0' ,
  tenant_id int DEFAULT '-1' ,
  execution_type int DEFAULT '0',
  create_time timestamp DEFAULT NULL ,
  update_time timestamp DEFAULT NULL ,
  PRIMARY KEY (id) ,
  CONSTRAINT process_definition_unique UNIQUE (name, project_code)
) ;
DROP SEQUENCE IF EXISTS t_ds_process_definition_id_sequence;
CREATE SEQUENCE  t_ds_process_definition_id_sequence;
ALTER TABLE t_ds_process_definition ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_process_definition_id_sequence');

--
-- Table structure for table t_ds_process_instance
--

DROP TABLE IF EXISTS t_ds_process_instance;
CREATE TABLE t_ds_process_instance (
  id serial NOT NULL  ,
  name varchar(255) DEFAULT NULL ,
  process_definition_code bigint DEFAULT NULL ,
  process_definition_version int DEFAULT NULL ,
  state int DEFAULT NULL ,
  recovery int DEFAULT NULL ,
  start_time timestamp DEFAULT NULL ,
  end_time timestamp DEFAULT NULL ,
  run_times int DEFAULT NULL ,
  host varchar(135) DEFAULT NULL ,
  command_type int DEFAULT NULL ,
  command_param text ,
  task_depend_type int DEFAULT NULL ,
  max_try_times int DEFAULT '0' ,
  failure_strategy int DEFAULT '0' ,
  warning_type int DEFAULT '0' ,
  warning_group_id int DEFAULT NULL ,
  schedule_time timestamp DEFAULT NULL ,
  command_start_time timestamp DEFAULT NULL ,
  global_params text ,
  process_instance_json text ,
  flag int DEFAULT '1' ,
  update_time timestamp NULL ,
  is_sub_process int DEFAULT '0' ,
  executor_id int NOT NULL ,
  history_cmd text ,
  dependence_schedule_times text ,
  process_instance_priority int DEFAULT NULL ,
  worker_group varchar(64) ,
  environment_code bigint DEFAULT '-1',
  timeout int DEFAULT '0' ,
  tenant_id int NOT NULL DEFAULT '-1' ,
  var_pool text ,
  dry_run int DEFAULT '0' ,
  next_process_instance_id int DEFAULT '0',
  restart_time timestamp DEFAULT NULL ,
  PRIMARY KEY (id)
) ;
create index process_instance_index on t_ds_process_instance (process_definition_code,id);
create index start_time_index on t_ds_process_instance (start_time,end_time);
DROP SEQUENCE IF EXISTS t_ds_process_instance_id_sequence;
CREATE SEQUENCE  t_ds_process_instance_id_sequence;
ALTER TABLE t_ds_process_instance ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_process_instance_id_sequence');


DROP TABLE IF EXISTS t_ds_process_task_relation;
CREATE TABLE t_ds_process_task_relation (
  id serial NOT NULL  ,
  name varchar(255) DEFAULT NULL ,
  project_code bigint DEFAULT NULL ,
  process_definition_code bigint DEFAULT NULL ,
  process_definition_version int DEFAULT NULL ,
  pre_task_code bigint DEFAULT NULL ,
  pre_task_version int DEFAULT '0' ,
  post_task_code bigint DEFAULT NULL ,
  post_task_version int DEFAULT '0' ,
  condition_type int DEFAULT NULL ,
  condition_params text ,
  create_time timestamp DEFAULT NULL ,
  update_time timestamp DEFAULT NULL ,
  PRIMARY KEY (id)
) ;
create index idx_code on t_ds_process_task_relation (project_code,process_definition_code);
DROP SEQUENCE IF EXISTS t_ds_process_task_relation_id_sequence;
CREATE SEQUENCE  t_ds_process_task_relation_id_sequence;
ALTER TABLE t_ds_process_task_relation ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_process_task_relation_id_sequence');


--
-- Table structure for table t_ds_project
--

DROP TABLE IF EXISTS t_ds_project;
CREATE TABLE t_ds_project (
  id serial NOT NULL  ,
  name varchar(100) DEFAULT NULL ,
  code bigint NOT NULL,
  description varchar(200) DEFAULT NULL ,
  user_id int DEFAULT NULL ,
  flag int DEFAULT '1' ,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP ,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP ,
  PRIMARY KEY (id)
) ;
create index user_id_index on t_ds_project (user_id);
DROP SEQUENCE IF EXISTS t_ds_project_id_sequence;
CREATE SEQUENCE  t_ds_project_id_sequence;
ALTER TABLE t_ds_project ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_project_id_sequence');


DROP TABLE IF EXISTS t_ds_task_definition;
CREATE TABLE t_ds_task_definition (
  id serial NOT NULL  ,
  code bigint NOT NULL,
  name varchar(255) DEFAULT NULL ,
  version int NOT NULL ,
  description text ,
  project_code bigint DEFAULT NULL ,
  user_id int DEFAULT NULL ,
  task_type varchar(50) DEFAULT NULL ,
  task_params text ,
  flag int DEFAULT NULL ,
  task_priority int DEFAULT NULL ,
  worker_group varchar(255) DEFAULT NULL ,
  environment_code bigint DEFAULT '-1',
  fail_retry_times int DEFAULT NULL ,
  fail_retry_interval int DEFAULT NULL ,
  timeout_flag int DEFAULT NULL ,
  timeout_notify_strategy int DEFAULT NULL ,
  timeout int DEFAULT '0' ,
  delay_time int DEFAULT '0' ,
  resource_ids text ,
  create_time timestamp DEFAULT NULL ,
  update_time timestamp DEFAULT NULL ,
  PRIMARY KEY (id)
) ;
create index task_definition_index on t_ds_task_definition (code,id);
DROP SEQUENCE IF EXISTS t_ds_task_definition_id_sequence;
CREATE SEQUENCE  t_ds_task_definition_id_sequence;
ALTER TABLE t_ds_task_definition ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_task_definition_id_sequence');

--
-- Table structure for table t_ds_task_instance
--

DROP TABLE IF EXISTS t_ds_task_instance;
CREATE TABLE t_ds_task_instance (
  id serial NOT NULL  ,
  name varchar(255) DEFAULT NULL ,
  task_type varchar(50) DEFAULT NULL ,
  task_code bigint NOT NULL,
  task_definition_version int DEFAULT NULL ,
  process_instance_id int DEFAULT NULL ,
  state int DEFAULT NULL ,
  submit_time timestamp DEFAULT NULL ,
  start_time timestamp DEFAULT NULL ,
  end_time timestamp DEFAULT NULL ,
  host varchar(135) DEFAULT NULL ,
  execute_path varchar(200) DEFAULT NULL ,
  log_path varchar(200) DEFAULT NULL ,
  alert_flag int DEFAULT NULL ,
  retry_times int DEFAULT '0' ,
  pid int DEFAULT NULL ,
  app_link text ,
  task_params text ,
  flag int DEFAULT '1' ,
  retry_interval int DEFAULT NULL ,
  max_retry_times int DEFAULT NULL ,
  task_instance_priority int DEFAULT NULL ,
  worker_group varchar(64),
  environment_code bigint DEFAULT '-1',
  environment_config text,
  executor_id int DEFAULT NULL ,
  first_submit_time timestamp DEFAULT NULL ,
  delay_time int DEFAULT '0' ,
  var_pool text ,
  dry_run int DEFAULT '0' ,
  PRIMARY KEY (id)
) ;
create index idx_task_instance_code_version on t_ds_task_instance (task_code, task_definition_version);
DROP SEQUENCE IF EXISTS t_ds_task_instance_id_sequence;
CREATE SEQUENCE  t_ds_task_instance_id_sequence;
ALTER TABLE t_ds_task_instance ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_task_instance_id_sequence');


--
-- Table structure for table t_ds_user
--

DROP TABLE IF EXISTS t_ds_user;
CREATE TABLE t_ds_user (
  id serial NOT NULL  ,
  user_name varchar(64) DEFAULT NULL ,
  user_password varchar(64) DEFAULT NULL ,
  user_type int DEFAULT NULL ,
  email varchar(64) DEFAULT NULL ,
  phone varchar(11) DEFAULT NULL ,
  tenant_id int DEFAULT NULL ,
  create_time timestamp DEFAULT NULL ,
  update_time timestamp DEFAULT NULL ,
  queue varchar(64) DEFAULT NULL ,
  state int DEFAULT 1 ,
  PRIMARY KEY (id)
);
comment on column t_ds_user.state is 'state 0:disable 1:enable';
DROP SEQUENCE IF EXISTS t_ds_user_id_sequence;
CREATE SEQUENCE  t_ds_user_id_sequence;
ALTER TABLE t_ds_user ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_user_id_sequence');
