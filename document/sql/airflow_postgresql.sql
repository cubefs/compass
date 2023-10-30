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

CREATE TABLE tb_task_instance (
    task_id character varying(250) NOT NULL,
    dag_id character varying(250) NOT NULL,
    run_id character varying(250) NOT NULL,
    map_index integer DEFAULT '-1'::integer NOT NULL,
    start_date timestamp DEFAULT NULL,
    end_date timestamp DEFAULT NULL,
    duration double precision,
    state character varying(20),
    try_number integer,
    max_tries integer DEFAULT '-1'::integer,
    hostname character varying(1000),
    unixname character varying(1000),
    job_id integer,
    pool character varying(256) NOT NULL,
    pool_slots integer NOT NULL,
    queue character varying(256),
    priority_weight integer,
    operator character varying(1000),
    queued_dttm timestamp DEFAULT NULL,
    queued_by_job_id integer,
    pid integer,
    executor_config bytea,
    updated_at timestamp DEFAULT NULL,
    external_executor_id character varying(250),
    trigger_id integer,
    trigger_timeout timestamp without time zone,
    next_method character varying(1000),
    next_kwargs json
);


CREATE TABLE tb_dag_run (
    id integer NOT NULL,
    dag_id character varying(250) NOT NULL,
    queued_at timestamp DEFAULT NULL,
    execution_date timestamp NOT NULL,
    start_date timestamp DEFAULT NULL,
    end_date timestamp DEFAULT NULL,
    state character varying(50),
    run_id character varying(250) NOT NULL,
    creating_job_id integer,
    external_trigger boolean,
    run_type character varying(50) NOT NULL,
    conf bytea,
    data_interval_start timestamp DEFAULT NULL,
    data_interval_end timestamp DEFAULT NULL,
    last_scheduling_decision timestamp DEFAULT NULL,
    dag_hash character varying(32),
    log_template_id integer,
    updated_at timestamp DEFAULT NULL
);


CREATE TABLE tb_ab_user (
    id integer NOT NULL,
    first_name character varying(64) NOT NULL,
    last_name character varying(64) NOT NULL,
    username character varying(256) NOT NULL,
    password character varying(256),
    active boolean,
    email character varying(256) NOT NULL,
    last_login timestamp without time zone,
    login_count integer,
    fail_login_count integer,
    created_on timestamp without time zone,
    changed_on timestamp without time zone,
    created_by_fk integer,
    changed_by_fk integer
);


CREATE TABLE tb_dag (
    dag_id character varying(250) NOT NULL,
    root_dag_id character varying(250),
    is_paused boolean,
    is_subdag boolean,
    is_active boolean,
    last_parsed_time timestamp DEFAULT NULL,
    last_pickled timestamp DEFAULT NULL,
    last_expired timestamp DEFAULT NULL,
    scheduler_lock boolean,
    pickle_id integer,
    fileloc character varying(2000),
    processor_subdir character varying(2000),
    owners character varying(2000),
    description text,
    default_view character varying(25),
    schedule_interval text,
    timetable_description character varying(1000),
    max_active_tasks integer NOT NULL,
    max_active_runs integer,
    has_task_concurrency_limits boolean NOT NULL,
    has_import_errors boolean DEFAULT false,
    next_dagrun timestamp DEFAULT NULL,
    next_dagrun_data_interval_start timestamp DEFAULT NULL,
    next_dagrun_data_interval_end timestamp DEFAULT NULL,
    next_dagrun_create_after timestamp DEFAULT NULL
);


CREATE TABLE tb_serialized_dag (
    dag_id character varying(250) NOT NULL,
    fileloc character varying(2000) NOT NULL,
    fileloc_hash bigint NOT NULL,
    data json,
    data_compressed bytea,
    last_updated timestamp DEFAULT NULL NOT NULL,
    dag_hash character varying(32) NOT NULL,
    processor_subdir character varying(2000)
);
