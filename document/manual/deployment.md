# Compass(罗盘) 部署指南

Compass 依赖了调度平台、Hadoop、Spark、Canal、MySQL、Kafka、Redis、Zookeeper、Elasticsearch，需要提前准备好相关环境。

Compass 支持单机和集群部署，可按模块弹性扩缩容。

## 工程目录

```
compass
├── bin
│   ├── compass_env.sh                  环境变量，基础组件配置
│   ├── start_all.sh                    启动脚本
│   └── stop_all.sh                     停止脚本
├── conf
│   └── application-hadoop.yml          hadoop相关配置
├── task-application                    关联任务实例、applicationId、hdfs_log_path
├── task-canal                          订阅调度平台MySQL表元数据到Kafka
├── task-canal-adapter                  同步调度平台MySQL表元数据Compass平台
├── task-detect                         工作流层异常类型检测
├── task-metadata                       同步Yarn、Spark任务元数据到Elasticsearch
├── task-parser                         日志解析和Spark任务异常检测
├── task-portal                         异常任务的可视化服务
└── task-syncer                         调度平台任务关系表的抽象和映射
```
### 初始化数据库

初始化数据库和表，请先执行document/sql/compass.sql

如果您使用的是DolphinScheduler调度平台，请执行document/sql/dolphinscheduler.sql

如果您使用的是Airflow调度平台，请执行document/sql/airflow.sql

如果您使用的是自研调度平台，请参考[task-syncer](#task-syncer)模块，确定需要同步的表


### 关键脚本和配置

compass/bin 和 compass/conf 是作为公共脚本和配置使用，方便统一启停和配置管理。

```
# 启动所有模块
./bin/start_all.sh
# 停止所有模块
./bin/stop_all.sh
```

#### compass_env.sh 配置说明

通过Environment属性绑定对应SpringBoot配置，只修改环境变量即可。

启动之前需要先确定调度平台类型、调度平台MySQL订阅账号、Compass MySQL、Kafka、Redis、Zookeeper、Elasticsearch集群地址

Kafka需要预先创建好topic: mysqldata,task-instance，按实际数据量设置分区

```bash
#!/bin/bash

# 调度平台选择：dolphinscheduler or airflow or custom
export SCHEDULER="dolphinscheduler"
export SPRING_PROFILES_ACTIVE="hadoop,${SCHEDULER}"


# 调度平台所使用的MySQL配置
export SCHEDULER_MYSQL_ADDRESS="ip:port"
export SCHEDULER_MYSQL_DB=""
export SCHEDULER_DATASOURCE_URL="jdbc:mysql://${SCHEDULER_MYSQL_ADDRESS}/${SCHEDULER_MYSQL_DB}?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai"
export SCHEDULER_DATASOURCE_USERNAME=""
export SCHEDULER_DATASOURCE_PASSWORD=""

# 提供给Compass使用的MySQL数据库配置
export COMPASS_MYSQL_ADDRESS="ip:port"
export COMPASS_MYSQL_DB=""
export SPRING_DATASOURCE_URL="jdbc:mysql://${COMPASS_MYSQL_ADDRESS}/${COMPASS_MYSQL_DB}?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai"
export SPRING_DATASOURCE_USERNAME=""
export SPRING_DATASOURCE_PASSWORD=""

# Kafka
export SPRING_KAFKA_BOOTSTRAPSERVERS="ip1:port,ip2:port"

# Redis
export SPRING_REDIS_CLUSTER_NODES="ip1:port,ip2:port"

# Zookeeper
export SPRING_ZOOKEEPER_NODES="ip1:port,ip2:port"

# Elasticsearch
export SPRING_ELASTICSEARCH_NODES="ip1:port,ip2:port"

# task-canal模块配置

# 调度平台MySQL订阅账号，确定是否已开启binlog
export CANAL_INSTANCE_MASTER_ADDRESS=${SCHEDULER_MYSQL_ADDRESS}
export CANAL_INSTANCE_DBUSERNAME=${SCHEDULER_DATASOURCE_USERNAME}
export CANAL_INSTANCE_DBPASSWORD=${SCHEDULER_DATASOURCE_PASSWORD}
# 需要订阅的库表配置过滤
if [ ${SCHEDULER} == "dolphinscheduler" ]; then
  export CANAL_INSTANCE_FILTER_REGEX="${SCHEDULER_MYSQL_DB}.t_ds_user,${SCHEDULER_MYSQL_DB}.t_ds_project,${SCHEDULER_MYSQL_DB}.t_ds_task_definition,${SCHEDULER_MYSQL_DB}.t_ds_task_instance,${SCHEDULER_MYSQL_DB}.t_ds_process_definition,${SCHEDULER_MYSQL_DB}.t_ds_process_instance,${SCHEDULER_MYSQL_DB}.t_ds_process_task_relation"
elif [ ${SCHEDULER} == "airflow" ]; then
  export CANAL_INSTANCE_FILTER_REGEX="${SCHEDULER_MYSQL_DB}.dag,${SCHEDULER_MYSQL_DB}.serialized_dag,${SCHEDULER_MYSQL_DB}.ab_user,${SCHEDULER_MYSQL_DB}.dag_run,${SCHEDULER_MYSQL_DB}.task_instance"
else
  export CANAL_INSTANCE_FILTER_REGEX=".*\\..*"
fi

```

#### application-hadoop.yml 说明

```
hadoop:
  # task-applicaiton & task-parser 模块配置依赖
  namenodes:
    - nameservices: logs-hdfs               # dfs.nameservices 属性值
      namenodesAddr: [ "machine1.example.com", "machine2.example.com" ]   # dfs.namenode.rpc-address.[nameservice ID].[name node ID] 属性值
      namenodes: ["nn1", "nn2"] # dfs.ha.namenodes.[nameservice ID] 属性值
      user: hdfs                            # 用户
      password:                             # 密码，如果没开启鉴权，则不需要
      port: 8020                            # 端口
      matchPathKeys: [ "flume" ]            # task-application模块使用，调度平台日志hdfs路径关键字
  
  # task-metadata 模块配置依赖
  yarn:
    - clusterName: "bigdata"
      resourceManager: [ "ip:port" ] # yarn.resourcemanager.webapp.address 属性值
      jobHistoryServer: "ip:port" # mapreduce.jobhistory.webapp.address 属性值
  spark:
    sparkHistoryServer: [ "ip:port" ] # spark history ui 地址
```



## task-canal

如果您使用的是[DolphinScheduler](https://github.com/apache/dolphinscheduler)
或者[Airflow](https://github.com/apache/airflow)或者自研的等调度平台，
元数据存储在MySQL，可使用[canal.deployer](https://github.com/alibaba/canal/releases/download/canal-1.1.6/canal.deployer-1.1.6.tar.gz)
订阅MySQL binlog同步到Kafka，默认topic是mysqldata

```
task-canal
├── bin
│   ├── compass_env.sh           compass环境变量
│   ├── init_canal.sh            下载canal.deployer依赖包
│   ├── restart.sh         
│   ├── startup.sh
│   └── stop.sh
├── canal.deployer-1.1.6.tar.gz   compass不提供canal依赖包，可通过init_canal.sh下载，若无网络则自行下载到task-canal根目录
├── conf
│   ├── mysqldata
│   │   ├── instance.properties   源MySQL配置和库表配置
│   ├── canal_local.properties    zk,kafka等配置
│   ├── canal.properties
│   ├── logback.xml
├── lib
└── plugin
```

### 核心配置

conf/mysqldata/instance.properties

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

[canal.adapter](https://github.com/alibaba/canal/releases/download/canal-1.1.6/canal.adapter-1.1.6.tar.gz)模块作用:
同步依赖调度平台的元数据表到compass，只同步任务相关和用户表，其他表按需同步

例如对于DolphinScheduler： t_ds_project.yml 定义同步了 ds_project表，若需要同步其他表可参考conf/rdb下配置

项目中已提供DolphinScheduler和Airflow平台同步模板，若使用其他平台可参考模板

```
task-canal-adapter
├── bin
│   ├── compass_env.sh
│   ├── init_canal_adapter.sh       下载canal.adapter压缩包和解压相关lib和plugin
│   ├── restart.sh
│   ├── startup.sh
│   └── stop.sh
├── canal.adapter-1.1.6.tar.gz      compass不提供canal依赖包，可通过init_canal.sh下载，若无网络则自行下载到task-canal-adapter根目录
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

### 表数据全量同步接口

示例：curl "localhost:8181/etl/rdb/mysql1/template.yml" -X POST

其中template.yml即为conf/rdb下的配置文件

### 核心配置

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

task-syncer模块是关联调度平台和compass的抽象层，使得compass能够兼容和诊断不同的调度平台任务，该模块抽象定义了compass核心依赖的关系表：

user：登录和权限校验，隔离不同用户权限

project：项目关系

flow：工作流定义关系

task：具体任务定义关系

task_instance：任务运行实例

其中关系是user -> project -> flow -> task -> task_instance，可根据实际调度平台自行定义关系

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

### 核心配置

conf/application-xxx.yml定义了数据同步表字段之间的映射关系，实现源表和目标表的转化，

columnMapping 实现了字段之间的映射

columnValueMapping 实现了字段值的映射

constantColumn 实现了常量列的映射

columnDep 实现了列字段值依赖查询，可自定义SQL实现表字段之间的关联

下面以同步DolphinScheduler调度平台示例说明

user表映射：

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

task_instance表映射：

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

task-application模块关联task_name、applicationId、hdfs_log_path，该模块需要读取调度平台日志，推荐使用flume收集到hdfs，方便统一做日志诊断和分析。

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

### 核心配置

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

conf/application-dolphinscheduler/airflow/custom.yml

该配置涉及日志路径规则的拼接，即日志绝对路径的确定。以flume收集dolphinscheduler到hdfs为例，airflow等同理。
表t_ds_task_instance记录了日志路径log_path,但这个是worker主机中的目录，上传到hdfs的目录有所变化。

例如:

scheduler worker log_path: /home/service/app/dolphinscheduler/logs/8590950992992_2/33552/33934.log

hdfs log_path: hdfs://log-hdfs:8020/flume/dolphinscheduler/2023-03-30/8590950992992_2/33552/xxx

因此需要根据上面的变化关系，通过逐级目录确定绝对路径，然后最终确定 task_name,application_id,hdfs_log_path
之间的关系存储到表task_application中。

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
        - { "column": "", "data": "hdfs://log-hdfs:8020/flume/dolphinscheduler" } # 相当于根目录，常量
        - { "column": "end_time", "regex": "^.*(?<date>\\d{4}-\\d{2}-\\d{2}).+$", "name": "date" }
        - { "column": "log_path", "regex": "^.*logs/(?<logpath>.*)$", "name": "logpath" }
      extractLog: # 根据组装的日志路径解析日志
        regex: "^.*Submitted application (?<applicationId>application_[0-9]+_[0-9]+).*$"     # 匹配规则
        name: "applicationId"      # 匹配文本名，最后必须有applicationId
```

注意：原生flume-taildir-source插件是不支持递归遍历子目录文件的，需要进行改造。如果您日志已经收集，可忽略。
如果您还没有收集，可修改TaildirMatcher.getMatchingFilesNoCache()方法实现。
如果你使用的是Airflow，生成的日志目录可能包含不符合hdfs目录规则，sink to hdfs时需要修改替换目录特殊字符为下划线‘_’。

## task-metadata

task-metadata模块是用于同步Yarn、Spark任务applicationId列表，关联applicationId的driver、executor、eventlog日志存储路径

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

### 核心配置

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

task-detect模块是针对工作流层异常检测，异常类型包括运行失败、基线时间异常、基线耗时异常、首次失败、长期失败、运行耗时长

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

### 核心配置

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

task-parser模块是针对Spark任务和相关日志进行解析诊断，异常类型包括：SQL失败、Shuffle失败、内存溢出、内存浪费、CPU浪费、大表扫描、OOM预警、
数据倾斜、Job耗时异常、Stage耗时异常、Task长尾、HDFS卡顿、推迟执行Task过多、全局排序异常等

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

### 核心配置

conf/rules.json 该配置是用于编写日志解析规则

logType: scheduler/driver/executor/yarn 日志类型，若有其他日志，可自行实现

action: 定义每个匹配规则名称

desc： action描述

category： 定义规则类型，例如shuffleFailed/sqlFailed等

step: 匹配顺序，默认升序

parserType: 匹配类型，默认 DEFAULT(按行或者块匹配)，JOIN(把结果合并成一行再匹配)

parserTemplate: 文本解析模板，由首行、中间行和结束行组成。

如果只是简单按行匹配，则只需要填写parserTemplate.heads即可；

如果需要按文本块匹配，例如异常栈，则需要填写parserTemplate.heads和parserTemplate.tails确定首行和结束行规则；

如果需要在文本块中匹配某一行，则需要填写parserTemplate.middles中间行规则。

groupNames：用户提取正则匹配分组名称的值

children: 用于嵌套规则，例如文本中有多个相同的异常栈(开始和结束标志一样)，如果需要区分成不同的action，那么就可以嵌套规则实现

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

conf/application.yml

custom.detector用于配置检测Spark Event Log，比如Spark环境变量、内存浪费、大表扫描等异常检测类型

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

task-portal 与 task-ui 可视化前后端模块，提供诊断建议、报告总览、一键诊断、任务运行、APP运行、白名单等服务

task-ui前端默认一起编译放在task-portal/portal目录下

如果您需要单独部署前端，需要修改 task-ui/src/utils/request.ts 下 baseURL，指定您的后端地址或者域名即可

web ui默认路径: http://localhost:7075/compass/

swagger ui默认路径：http://localhost:7075/compass/swagger-ui/index.html
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