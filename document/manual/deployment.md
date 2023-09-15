# Compass Deployment

Compass depends on Canal,MySQL,Kafka,Redis,Zookeeper,OpenSearch
## Required Environment 
|Dependency|Version|Optional|Description|
|----------|-------|--------|----|
|Canal|v1.1.6+|yes| needed by Airflow,DolphinScheduler|
|MySQL|5.7+|no||
|Kafka|all|no||
|Redis|all|no|deployed in cluster mode|
|Zookeeper|3.4.5|no|needed by canal|
|OpenSearch(Elasticsearch)|1.3.12 (7.0+ for es)|no||

Compass supports single-machine and cluster deployment, with elastic scalability by module.

## Compile
Use JDK 8 and maven 3.6.0+ to Compile
```
git clone https://github.com/cubefs/compass.git
cd compass
mvn package -DskipTests
```

## Workspaces

```
compass
├── bin
│   ├── compass_env.sh                  global variable configuration
│   ├── start_all.sh                    start script
│   └── stop_all.sh                     stop script
├── conf
│   └── application-hadoop.yml          hadoop configuration
├── task-application                    Related to task instance、applicationId、hdfs_log_path
├── task-canal                          Synchronize scheduler metadata from MySQL to Kafka
├── task-canal-adapter                  Synchronzie scheduler metadata from MySQL to Compass
├── task-detect                         Detect the job from scheduler
├── task-metadata                       Syncrhonize metadata from Yarn、Spark to OpenSearch
├── task-parser                         Log parse and spark abnormal task detect
├── task-portal                         Visualizaiton web service for analysis
├── task-flink                          Flink task resources and exception diagnosis
├── task-flink-core                     Flink task diagnosis rule
└── task-syncer                         Abstract and map the task relationship table of the scheduling platform
```
### Initialize database

Initialize the database and tables, please execute document/sql/compass.sql first

If you are using the DolphinScheduler, please execute document/sql/dolphinscheduler.sql (need to be modified according to the actual version used, supporting 2.x and 3.x).

If you are using the Airflow, please execute document/sql/airflow.sql (need to be modified according to the actual version used)

If you are using a self-developed scheduling platform, please refer to the [task-syncer](#task-syncer) module to determine which tables need to be synchronized.


### Configuration

compass/bin and compass/conf are used as public scripts and configurations to facilitate unified startup and configuration management.

```
# Start all modules
./bin/start_all.sh
# Stop all modules
./bin/stop_all.sh
```

**Note: If you have not used ./bin/start_all.sh and have adjusted the configuration of each module separately, you need to copy compass_env.sh file to the bin directory of each module and copy application-hadoop.yml to the conf directory of the task-application, task-metadata, and task-parser modules.**

**What is compass_env.sh? It is a common configuration file for all modules. If you need to quickly start the modules, you only need to modify this script and leave the other configurations as default. If you need to adjust the configuration for optimization, then you can modify the specific configuration.**

**What is application-hadoop.yml? It is a configuration file for dependencies (namenode, yarn, spark) and will be copied to the dependent modules when star_all.sh is executed.**

#### compass_env.sh Explanation

Bind corresponding SpringBoot configuration through Environment properties, and modify only the environment variables.

Before starting, you need to determine the scheduling platform type, the MySQL subscription account for the scheduling platform, Compass MySQL, Kafka, Redis, Zookeeper, and OpenSearch cluster addresses.

Kafka needs to have the topics 'mysqldata', 'task-instance', and 'task-application' created in advance, and the number of partitions should be set according to the actual data volume.

```bash
#!/bin/bash

# Scheduling platform selection: Dolphinscheduler or Airflow or Custom
export SCHEDULER="dolphinscheduler"
export SPRING_PROFILES_ACTIVE="hadoop,${SCHEDULER}"


# MySQL configuration used by the scheduling platform
export SCHEDULER_MYSQL_ADDRESS="ip:port"
export SCHEDULER_MYSQL_DB=""
export SCHEDULER_DATASOURCE_URL="jdbc:mysql://${SCHEDULER_MYSQL_ADDRESS}/${SCHEDULER_MYSQL_DB}?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai"
export SCHEDULER_DATASOURCE_USERNAME=""
export SCHEDULER_DATASOURCE_PASSWORD=""

# MySQL database configuration provided for use by Compass
export COMPASS_MYSQL_ADDRESS="ip:port"
export COMPASS_MYSQL_DB=""
export SPRING_DATASOURCE_URL="jdbc:mysql://${COMPASS_MYSQL_ADDRESS}/${COMPASS_MYSQL_DB}?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai"
export SPRING_DATASOURCE_USERNAME=""
export SPRING_DATASOURCE_PASSWORD=""

# Kafka (default version: 3.4.0)
export SPRING_KAFKA_BOOTSTRAPSERVERS="ip1:port,ip2:port"

# Redis (cluster mode)
export SPRING_REDIS_CLUSTER_NODES="ip1:port,ip2:port"

# Zookeeper (cluster: 3.4.5, needed by canal)
export SPRING_ZOOKEEPER_NODES="ip1:port,ip2:port"

# OpenSearch (default version: 1.3.12) or Elasticsearch (7.x~)
export SPRING_OPENSEARCH_NODES="ip1:port,ip2:port"
# Optional
export SPRING_OPENSEARCH_USERNAME=""
# Optional
export SPRING_OPENSEARCH_PASSWORD=""
# Optional, needed by OpenSearch, keep empty if OpenSearch does not use truststore.
export SPRING_OPENSEARCH_TRUSTSTORE=""
# Optional, needed by OpenSearch, keep empty if OpenSearch does not use truststore.
export SPRING_OPENSEARCH_TRUSTSTOREPASSWORD=""

# Flink metric prometheus
export FLINK_PROMETHEUS_HOST="host"
export FLINK_PROMETHEUS_TOKEN=""
export FLINK_PROMETHEUS_DATABASE=""

# task-canal configuration

# MySQL subscription account for the scheduling platform, and confirmation whether binlog has been enabled
export CANAL_INSTANCE_MASTER_ADDRESS=${SCHEDULER_MYSQL_ADDRESS}
export CANAL_INSTANCE_DBUSERNAME=${SCHEDULER_DATASOURCE_USERNAME}
export CANAL_INSTANCE_DBPASSWORD=${SCHEDULER_DATASOURCE_PASSWORD}
# Filtering is required for the subscribed library and tables
if [ ${SCHEDULER} == "dolphinscheduler" ]; then
  export CANAL_INSTANCE_FILTER_REGEX="${SCHEDULER_MYSQL_DB}.t_ds_user,${SCHEDULER_MYSQL_DB}.t_ds_project,${SCHEDULER_MYSQL_DB}.t_ds_task_definition,${SCHEDULER_MYSQL_DB}.t_ds_task_instance,${SCHEDULER_MYSQL_DB}.t_ds_process_definition,${SCHEDULER_MYSQL_DB}.t_ds_process_instance,${SCHEDULER_MYSQL_DB}.t_ds_process_task_relation"
elif [ ${SCHEDULER} == "airflow" ]; then
  export CANAL_INSTANCE_FILTER_REGEX="${SCHEDULER_MYSQL_DB}.dag,${SCHEDULER_MYSQL_DB}.serialized_dag,${SCHEDULER_MYSQL_DB}.ab_user,${SCHEDULER_MYSQL_DB}.dag_run,${SCHEDULER_MYSQL_DB}.task_instance"
else
  export CANAL_INSTANCE_FILTER_REGEX=".*\\..*"
fi

```

#### application-hadoop.yml configuration

```
hadoop:
  # Configuration dependency for task-application & task-parser modules
  namenodes:
    - nameservices: logs-hdfs               # dfs.nameservices value
      namenodesAddr: [ "machine1.example.com", "machine2.example.com" ]   # dfs.namenode.rpc-address.[nameservice ID].[name node ID] value
      namenodes: ["nn1", "nn2"] # dfs.ha.namenodes.[nameservice ID] value
      user: hdfs                            # user
      password:                             # password，empty if no password
      port: 8020                            # port
      matchPathKeys: [ "flume" ]            # Usage of the task-application module, keywords for the HDFS path of the scheduling platform log
      # kerberos
      enableKerberos: false
      # /etc/krb5.conf
      krb5Conf: ""
      # hdfs/*@EXAMPLE.COM
      principalPattern:  ""
      # admin
      loginUser: ""
      # /var/kerberos/krb5kdc/admin.keytab
      keytabPath: ""
  
  # Configuration dependency for the task-metadata module
  yarn:
    - clusterName: "bigdata"
      resourceManager: [ "ip:port" ] # yarn.resourcemanager.webapp.address 
      jobHistoryServer: "ip:port" # mapreduce.jobhistory.webapp.address 
  spark:
    sparkHistoryServer: [ "ip:port" ] # spark history ui address
```

## task-canal

If you are using [DolphinScheduler](https://github.com/apache/dolphinscheduler)
or [Airflow](https://github.com/apache/airflow) or self-developed scheduling platform,
Metadata is stored in MySQL，use [canal.deployer](https://github.com/alibaba/canal/releases/download/canal-1.1.6/canal.deployer-1.1.6.tar.gz)
to subscribe to MySQL binlog and synchronize to Kafka. The default topic is 'mysqldata'.

```
task-canal
├── bin
│   ├── compass_env.sh           compass global variable
│   ├── init_canal.sh            dwonload canal.deployer dependency
│   ├── restart.sh         
│   ├── startup.sh
│   └── stop.sh
├── canal.deployer-1.1.6.tar.gz   Compass does not provide the Canal dependency package, which can be downloaded through init_canal.sh. If there is no network, it can be downloaded to the root directory of task-canal manually.
├── conf
│   ├── example
│   │   ├── instance.properties   Source table configuration
│   ├── canal_local.properties    zk,kafka configuration
│   ├── canal.properties
│   ├── logback.xml
├── lib
└── plugin
```

### core configuration

conf/example/instance.properties

```
canal.instance.master.address=localhost:33066
canal.instance.dbUsername=root
canal.instance.dbPassword=root
canal.instance.filter.regex=.*\\..*
canal.mq.topic=mysqldata

# 动态topic和分区默认不配置，若数据量比较大，可按表Hash到相同topic不同分区，避免单分区压力过大
canal.mq.dynamicTopic = mysqldata:db\\..*
canal.mq.partitionsNum =  12
canal.mq.partitionHash = .*\\..*
```

conf/canal.properties

```
canal.zkServers = localhost:2181
canal.serverMode = kafka
kafka.bootstrap.servers = localhost:9092
```

## task-canal-adapter

[canal.adapter](https://github.com/alibaba/canal/releases/download/canal-1.1.6/canal.adapter-1.1.6.tar.gz):
Synchronize metadata tables from the scheduling platform to Compass, and only synchronize the task and user tables. Other tables can be synchronized as needed.

For example, for DolphinScheduler: the t_ds_project.yml defines the synchronization of the `ds_project` table. If you need to synchronize other tables, you can refer to the configuration in `conf/rdb`

The project has provided synchronization templates for both DolphinScheduler and Airflow platforms. If you are using other platforms, you can refer to these templates.
```
task-canal-adapter
├── bin
│   ├── compass_env.sh
│   ├── init_canal_adapter.sh       Download the Canal.adapter package and extract the relevant lib and plugin files.
│   ├── restart.sh
│   ├── startup.sh
│   └── stop.sh
├── canal.adapter-1.1.6.tar.gz       Compass does not provide the Canal dependency package, which can be downloaded through init_canal.sh. If there is no network, it can be downloaded to the root directory of task-canal-adapter manually.
├── conf
│   ├── application.yml
│   └── rdb
│       ├── airflow_db_ab_user.yml
│       ├── airflow_db_dag_run.yml
│       ├── airflow_db_dag.yml
│       ├── airflow_db_task_instance.yml
│       ├── t_ds_process_definition.yml
│       ├── t_ds_process_instance.yml
│       ├── t_ds_process_task_relation.yml
│       ├── t_ds_project.yml
│       ├── t_ds_task_definition.yml
│       ├── t_ds_task_instance.yml
│       ├── t_ds_user.yml
│       └── template.yml
├── lib
└── plugin
```

### Full table data synchronization interface

For example：curl "localhost:8181/etl/rdb/mysql1/template.yml" -X POST

The `template.yml` is the configuration file under `conf/rdb`.

### Conguration

conf/application.yml

```
canal.conf:
  srcDataSources:
    defaultDS:
      # 调度平台MySQL同步账号
      url: ${CANAL_ADAPTER_SOURCE_MYSQL_URL}
      username: ${CANAL_ADAPTER_SOURCE_MYSQL_USERNAME}
      password: ${CANAL_ADAPTER_SOURCE_MYSQL_PASSWORD}

  canalAdapters:
  - instance: mysqldata # kafka topic
    groups:
    - groupId: g1
      outerAdapters:
      - name: rdb
        key: mysql1
        properties:
          jdbc.driverClassName: com.mysql.jdbc.Driver
          # compass平台MySQL账号
          jdbc.url: ${CANAL_ADAPTER_DESTINATION_MYSQL_URL}
          jdbc.username: ${CANAL_ADAPTER_DESTINATION_MYSQL_USERNAME}
          jdbc.password: ${CANAL_ADAPTER_DESTINATION_MYSQL_PASSWORD}
```

conf/rdb/template.yml

```
dataSourceKey: defaultDS
destination: mysqldata
groupId: g1
outerAdapterKey: mysql1
concurrent: false
dbMapping:
  database: ${SCHEDULER_MYSQL_DB}
  # 调度平台MySQL表
  table: example
  # compass平台MySQL表
  targetTable: example
  # 主键配置
  targetPk:
    id: id
  mapAll: true
  commitBatch: 1
```

## task-syncer

The task-syncer module is the abstraction layer that connects the scheduling platform and Compass, enabling Compass to be compatible with and diagnose different scheduling platform tasks. This module abstractly defines the relationship table that is the core dependency of Compass:

user：Login and permission verification, isolating different user permissions

project：Project definition

flow：Workflow definition 

task：Task definition

task_instance：Task running instance

The relationship: user -> project -> flow -> task -> task_instance, relationships can be defined according to the actual scheduling platform.

```
task-syncer
├── bin
│   ├── compass_env.sh
│   ├── startup.sh
│   └── stop.sh
├── conf
│   ├── application-airflow.yml
│   ├── application-dolphinscheduler.yml
│   ├── application.yml
│   └── logback.xml
├── lib
```

### Configuraion

`conf/application-xxx.yml` defines the mapping relationship between the fields of data synchronization tables, which achieves the conversion between source tables and target tables,

`columnMapping` achieves the mapping between fields.

`columnValueMapping` achieves the mapping of field values.

`constantColumn` achieves the mapping of constant columns.

`columnDep` implements the column value dependency query and allows custom SQL to establish associations between table columns.

Below is an example of synchronizing DolphinScheduler scheduling platform:

user table：

```
# DolphinScheduler库名
- schema: "dolphinscheduler" 
  # DolphinScheduler user表
  table: "t_ds_user"
  # compass user表        
  targetTable: "user"
  # columnMapping用于字段映射，key是compass定义的字段，值是DolphinScheduler定义的字段   
  columnMapping: 
    user_id: "id"           
    username: "user_name"
    password: "user_password"
    is_admin: "user_type"
    email: "email"
    phone: "phone"
    create_time: "create_time"
    update_time: "update_time"
  # 字段值映射, 目标字段值, 源字段值, 字段类型
  columnValueMapping:
    is_admin: [ { targetValue: "0", originValue: [ "0" ] }, { targetValue: "1", originValue: [ "1" ] } ]
  # 常量列定义
  constantColumn:
    scheduler_type: "DolphinScheduler"
```

task_instance table：

```
- schema: "dolphinscheduler"
  table: "t_ds_task_instance"
  targetTable: "task_instance"
  columnMapping:
    id: "id"
    project_name: ""
    flow_name: ""
    task_name: "name"
    start_time: "start_time"
    end_time: "end_time"
    execution_time: ""
    task_state: "state"
    task_type: "task_type"
    retry_times: "retry_times"
    max_retry_times: "max_retry_times"
    worker_group: "worker_group"
    create_time: "create_time"
    update_time: "update_time"
  columnValueMapping:
    task_state:
      - { targetValue: "success", originValue: [ "7", "14" ] }
      - { targetValue: "fail", originValue: [ "6", "9" ] } 
      - { targetValue: "other", originValue: [ "0", "1", "2", "3", "4", "5", "8", "10", "11", "12", "13" ] }
  columnDep:
    # 列字段值依赖，由于该表缺失了project_name, flow_name, execution_time字段，因此需要关联其他表查询
    columns: [ "project_name", "flow_name", "execution_time" ]
    queries: [ "select t2.schedule_time as execution_time, t3.name as flow_name, t4.name as project_name from t_ds_task_instance as t1 inner join t_ds_process_instance as t2 on t1.process_instance_id = t2.id inner join t_ds_process_definition as t3 on t2.process_definition_code = t3.code inner join t_ds_project as t4 on t3.project_code=t4.code where t1.id=${id}" ]
```

## task-application

The task-application module associates task_name, applicationId, and hdfs_log_path. This module needs to read the scheduling platform logs, and it is recommended to collect them to HDFS using Flume for the convenience of unified log diagnosis and analysis.

```
task-application/
├── bin
│   ├── compass_env.sh
│   ├── startup.sh
│   └── stop.sh
├── conf
│   ├── application-airflow.yml
│   ├── application-dolphinscheduler.yml
│   ├── application-hadoop.yml
│   ├── application.yml
│   └── logback.xml
├── lib
```

### Configuraion

conf/application-hadoop.yml

```
hadoop:
  namenodes:
    - nameservices: logs-hdfs
      namenodesAddr: [ "host1", "host2" ]
      namenodes: ["namenode1", "namenode2"]
      user: hdfs
      password:
      port: 8020
      matchPathKeys: [ "flume" ]
```

`conf/application-dolphinscheduler/airflow/custom.yml`

This configuration involves the concatenation of log path rules, specifically determining the absolute path of the logs. Taking the example of collecting dolphinscheduler logs to HDFS using Flume, the same logic applies to Airflow.

The table t_ds_task_instance records the log path (log_path). However, this is the directory on the worker host, and the directory changes when uploading to HDFS.

For example:

scheduler worker log_path: /home/service/app/dolphinscheduler/logs/8590950992992_2/33552/33934.log

hdfs log_path: hdfs://log-hdfs:8020/flume/dolphinscheduler/2023-03-30/8590950992992_2/33552/xxx

Therefore, based on the above relationship changes, the absolute path is determined through step-by-step directory identification, and then the relationship between task_name, application_id, and hdfs_log_path is finally determined and stored in the task_application table.

```
custom:
  # 从上到下串行执行解析到任务的applicationId
  rules:
    - logPathDep:
        # 变量依赖查询
        query: "select CASE WHEN end_time IS NOT NULL THEN DATE_ADD(end_time, INTERVAL 1 second) ELSE start_time END as end_time,log_path from t_ds_task_instance where id=${id}"     # 查询, id 是 task-instance表的id
      logPathJoins: 
        # end_time: 2023-02-18 01:43:11
        # log_path: ../logs/6354680786144_1/3/4.log
        - { "column": "", "data": "/flume/dolphinscheduler" } # 配置存储调度日志的hdfs根目录
        - { "column": "end_time", "regex": "^.*(?<date>\\d{4}-\\d{2}-\\d{2}).+$", "name": "date" }
        - { "column": "log_path", "regex": "^.*logs/(?<logpath>.*)$", "name": "logpath" }
      extractLog: # 根据组装的日志路径解析日志
        regex: "^.*Submitted application (?<applicationId>application_[0-9]+_[0-9]+).*$"     # 匹配规则
        name: "applicationId"      # 匹配文本名，最后必须有applicationId
```

Note: The native Flume-taildir-source plugin does not support recursively traversing subdirectory files, and requires modification. If your logs have already been collected, you can ignore this.

If you have not collected logs, you can modify the TaildirMatcher.getMatchingFilesNoCache() method to implement this function. If you are using Airflow, the generated log directory may contain characters that do not comply with the HDFS directory rules. When sinking to HDFS, you need to modify and replace directory special characters with underscores ('_').
## task-metadata

The task-metadata module is used to synchronize the Yarn and Spark task applicationId lists and associate the storage paths of driver, executor, and eventlog logs with applicationId.
```
task-metadata
├── bin
│   ├── compass_env.sh
│   ├── startup.sh
│   └── stop.sh
├── conf
│   ├── application.yml
│   └── logback.xml
├── lib
```

### Configuration

conf/application.yml

```
hadoop:
  yarn:
    - clusterName: "bigdata"
      resourceManager: [ "ip:port" ]
      jobHistoryServer: "ip:port"

  spark:
    sparkHistoryServer: [ "ip:port" ]
```

## task-detect

The task-detect module is designed for abnormal detection at the workflow level. The types of abnormalities include running failure, baseline time exception, baseline duration exception, first-time failure, long-term failure, and long running time.
```
task-detect
├── bin
│   ├── compass_env.sh
│   ├── startup.sh
│   └── stop.sh
├── conf
│   ├── application.yml
├── lib
```

### Configuration

conf/application.yml

```
custom:
  detectionRule:
    # 运行耗时长配置，单位小时
    durationWarning: 2
    # 长期失败配置，单位天
    alwaysFailedWarning: 10 
```

## task-parser

The task-parser module is designed to parse and diagnose Spark tasks and related logs. The types of exceptions include SQL failure, Shuffle failure, memory overflow, memory waste, CPU waste, large table scans, OOM warnings, data skewness, abnormal Job duration, abnormal Stage duration, long tail of tasks, HDFS lag, excessive delayed execution of tasks, and global sorting anomalies.

```
task-parser
├── bin
│   ├── compass_env.sh
│   ├── startup.sh
│   └── stop.sh
├── conf
│   ├── applicationbk.yml
│   ├── application-hadoop.yml
│   ├── application.yml
│   ├── logback.xml
│   ├── rules.json
│   └── scripts
│       └── logRecordConsumer.lua
├── lib
```

### Configuration

The `conf/rules.json` configuration is used to write log parsing rules.
The fields are defined as following:

**logType**: scheduler/driver/executor/yarn 

**action**: Define the name of each matching rule.

**desc**： Description for action

**category**： Definition of rule types, such as shuffleFailed/sqlFailed, etc.

**step**: order of matching action.

**parserType**: Match type, DEFAULT(match by rows or blocks), JOIN(Merge results into one row before matching.)

**parserTemplate**: Text parsing templates consist of the first line, middle lines, and ending lines.

If only simple line matching is required, it is sufficient to fill in **parserTemplate.heads**.

If text block matching is required, such as for exception stacks, it is necessary to fill in **parserTemplate.heads** and **parserTemplate.tails** to determine the rules for the first line and the ending line.

If a specific line needs to be matched within a text block, the middle line rule in **parserTemplate.middles** must be filled in.

**groupNames**: Extracting values of named capturing groups in regular expression matching by users.

**children**: Used for nested rules, for example, when there are multiple identical exception stacks in the text (with the same start and end markers), if it is necessary to differentiate them into different actions, nested rules can be used to achieve this

For example:
```
  { 
    "logType": "scheduler",
    "actions": [
      {
        "action": "otherError",
        "desc": "其他错误信息",
        "category": "otherException",
        "step": 1,
        "skip": false,
        "parserType": "DEFAULT",
        "parserTemplate": {
          "heads": [
            "^.+ERROR.+$"
          ],
          "middles": [],
          "tails": []
        },
        "groupNames": [],
        "children": []
      }
    ]
  }
```

`conf/application.yml`

"custom.detector" is used to configure custom detectors for monitoring Spark event logs, such as detecting abnormalities related to Spark environment variables, memory wastage, large table scans, etc.

```
custom:
  detector:
    sparkEnvironmentConfig:
      jvmInformation:
        - Java Version
      sparkProperties:
        - spark.driver.memoryOverhead
        - spark.driver.memory
        - spark.executor.memoryOverhead
        - spark.executor.memory
        - spark.executor.cores
        - spark.dynamicAllocation.maxExecutors
        - spark.default.parallelism
        - spark.sql.shuffle.partitions
      systemProperties:
        - sun.java.command
     ...
```

## task-portal 与 task-ui

**task-portal** and **task-ui** are visual front-end and back-end modules that provide services such as diagnostic recommendations, report overviews, one-click diagnosis, task execution, APP execution, and whitelist.

The task-ui front-end is compiled together by default and placed in the task-portal/portal directory.

If you need to deploy the front-end separately, you need to modify VITE_APP_PROD_BACKEND in task-ui/.env.production to specify your backend address or domain name.

WebUI default http path: http://localhost:7075/compass/

SwaggerUI default http path：http://localhost:7075/compass/swagger-ui/index.html

About the **username** and **password** ：

If you are using DolphinScheduler or Airflow scheduling platform, that is configured in compass_env.sh using 'export SCHEDULER="dolphinscheduler/airflow"', the account password will be the same as the scheduling platform (data synchronization is required).

If you are using a self-developed or testing scheduling platform, please set 'export SCHEDULER="custom"' in compass_env.sh. After executing document/sql/compass.sql, the default account and password are 'compass, compass'. This mode does not perform account and password verification. Please pay attention to data security.
```
task-portal
├── bin
│   ├── compass_env.sh
│   ├── startup.sh
│   └── stop.sh
├── conf
├── lib
├── portal
│   ├── assets
│   └── index.html

```

## Offline task metadata reporting diagnosis.

Supports third-party reporting of Spark/MapReduce task application metadata for diagnosis. If you don't need to synchronize scheduling platform metadata and logs, simply start the task-portal and task-parser modules.

Request API：http://[compass_host]/compass/openapi/offline/app/metadata

Request Method:  POST

ContentType: application/json

Metadata comes from **http://rm-http-address:port/ws/v1/cluster/apps**

parameter				| data type		     | optional	            | description  
:----				|:---------|:-----------------|:---	
applicationId				| String		 | Yes			             | YARN application id
applicationType				| String		 | Yes			             | YARN App Type：SPARK or MAPREDUCE
vcoreSeconds			| Double		    | Yes			             | YARN vcoreSeconds
memorySeconds			| Double		   | Yes			             | YARN memorySeconds
startedTime			| Long		   | Yes			             | YARN startedTime
finishedTime			| Long		   | Yes			             | YARN finishedTime
elapsedTime			| Double		 | Yes			             | YARN elapsedTime
amHostHttpAddress			| String		 | Yes			             | YARN amHostHttpAddress
sparkEventLogFile			| String		 | Yes for SPARK			     | SparkEventLog absolutely log path
sparkExecutorLogDirectory			| String		 | Yes for SPARK			     | application id folder
mapreduceEventLogDirectory			| String		 | Yes for MAPREDUCE			 | folder 
mapreduceContainerLogDirectory			| String		 |Yes for MAPREDUCE			     | application id folder
diagnostics			| String		 | No			             | YARN diagnostics
queue			| String		 | No			             | YARN queue
user			| String		 | No			             | YARN user
clusterName			| String		 | No			             | cluster name

For example：
```json
{
    "applicationId": "application_1673850090992_30536",
    "applicationType": "SPARK",
    "vcoreSeconds": 550,
    "memorySeconds": 77079,
    "startedTime": 1692611101256,
    "finishedTime": 1692617022920,
    "elapsedTime": 35419,
    "amHostHttpAddress": "dgtest01:8043",
    "sparkEventLogFile": "hdfs://logs-cluster/user/spark/applicationHistory/application_1673850090992_30536_1",
    "sparkExecutorLogDirectory": "hdfs://logs-cluster/tmp/logs/hdfs/logs/application_1673850090992_30536",
    "mapreduceEventLogDirectory": "hdfs://logs-cluster/tmp/hadoop-yarn/staging/history/done", // for MAPREDUCE
    "mapreduceContainerLogDirectory": "hdfs://logs-cluster/tmp/logs/root/logs/application_1673850090992_30536", // for MAPREDUCE
    "diagnostics": "",
    "queue": "root",
    "user": "root",
    "clusterName": "test"
}
```


## One-Click diagnosis

Offline diagnosis supports one-click diagnosis for all Spark/MapReduce tasks, including those not submitted to the scheduling platform. If you only want to experience this function, simply start the task-portal, task-metadata, and task-parser modules.