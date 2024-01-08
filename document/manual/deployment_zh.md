# Compass(罗盘) 部署指南

[使用Docker Compose部署](../../docker/playground/README.md)

Compass 依赖了Canal、PostgreSQL(或MySQL)、Kafka、Redis、Zookeeper、OpenSearch，需要提前准备好相关环境。

## 环境要求
| Dependency | Version | Optional | Description                        |
|------------|---------|----------|------------------------------------|
| Canal      | v1.1.6+ | yes      | needed by Airflow,DolphinScheduler |
| MySQL      | 5.7+    | yes      ||
| PostgreSQL | 10.0+   | no       ||
| Kafka      | all     | no       ||
| Redis      | all     | no       | deployed in cluster mode           |
| Zookeeper  | 3.4.5   | no       | needed by canal                    |
| OpenSearch | 1.3.12  | no       ||

OpenSearch兼容Elasticsearch 7.0+。

Compass 支持单机和集群部署，可按模块弹性扩缩容。

## 编译
请使用`JDK 8`以及`maven 3.6.0+`进行编译，构建流程步骤如下：
```
git clone https://github.com/cubefs/compass.git
cd compass
mvn clean package -DskipTests -Pdist
或者
mvn clean package -DskipTests -Pdist,spark (打包时web展示只有spark诊断页面)
或者
mvn clean package -DskipTests -Pdist,flink (打包时web展示只有flink诊断页面)
```
使用docker compose启动应用服务
```
cp dist/compass-v1.1.2.tar.gz docker/playground
cd docker/playground/
docker compose --profile dependencies up -d
docker compose --profile compass-demo up -d
```
更多请查看文档 [docker-playground](https://github.com/cubefs/compass/blob/main/docker/playground/README.md)

## 初始化数据库

支持 PostgreSQL（默认） 或 MySQL（需要手动下载[mysql-connector-java](https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.29/mysql-connector-java-8.0.29.jar)复制到各模块lib目录下,canal除外） 作为元数据存储。

表结构由两部分组成： document/sql/compass_*.sql，document/sql/dolphinscheduler_*.sql（需要根据实际使用版本修改，支持2.x和3.x）或document/sql/airflow_*.sql（支持2.x）。

如果您使用的是自研调度平台，请参考上述sql表结构。

## 修改配置和启动

compass/bin 和 compass/conf 是作为公共脚本和配置使用，方便统一启停和配置管理。

```
# 启动所有模块
./bin/start_all.sh
# 停止所有模块
./bin/stop_all.sh
```

### compass_env.sh 配置说明

Kafka需要预先创建好topic: mysqldata,task-instance,task-application,exception-log

```bash
#!/bin/bash

# dolphinscheduler or airflow or custom
export SCHEDULER="dolphinscheduler"
export SPRING_PROFILES_ACTIVE="hadoop,${SCHEDULER}"

# Configuration for Scheduler MySQL, compass will subscribe data from scheduler database via canal
export SCHEDULER_MYSQL_ADDRESS="localhost:3306"
export SCHEDULER_MYSQL_DB="dolphinscheduler"
export SCHEDULER_DATASOURCE_URL="jdbc:mysql://${SCHEDULER_MYSQL_ADDRESS}/${SCHEDULER_MYSQL_DB}?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai"
export SCHEDULER_DATASOURCE_USERNAME=""
export SCHEDULER_DATASOURCE_PASSWORD=""

# Configuration for compass database(mysql or postgresql)
export DATASOURCE_TYPE="mysql"
export COMPASS_DATASOURCE_ADDRESS="localhost:3306"
export COMPASS_DATASOURCE_DB="compass"
export SPRING_DATASOURCE_URL="jdbc:${DATASOURCE_TYPE}://${COMPASS_DATASOURCE_ADDRESS}/${COMPASS_DATASOURCE_DB}"
export SPRING_DATASOURCE_USERNAME=""
export SPRING_DATASOURCE_PASSWORD=""

# Configuration for compass Kafka, used to subscribe data by canal and log queue, etc. (default version: 3.4.0)
export SPRING_KAFKA_BOOTSTRAPSERVERS="host1:port,host2:port"

# Configuration for compass redis, used to cache and log queue, etc . (cluster mode)
export SPRING_REDIS_CLUSTER_NODES="localhost:6379"
# Optional
export SPRING_REDIS_PASSWORD=""

# Zookeeper (cluster: 3.4.5, needed by canal)
export SPRING_ZOOKEEPER_NODES="localhost:2181"

# OpenSearch (default version: 1.3.12) or Elasticsearch (7.x~)
export SPRING_OPENSEARCH_NODES="localhost:9200"
# Optional
export SPRING_OPENSEARCH_USERNAME=""
# Optional
export SPRING_OPENSEARCH_PASSWORD=""
# Optional, needed by OpenSearch, keep empty if OpenSearch does not use truststore.
export SPRING_OPENSEARCH_TRUSTSTORE=""
# Optional, needed by OpenSearch, keep empty if OpenSearch does not use truststore.
export SPRING_OPENSEARCH_TRUSTSTOREPASSWORD=""

# spark.io.compression.codec: lz4/snappy/zstd
export CUSTOM_SPARK_COMPRESSIONCODEC=""

# Prometheus for flink, ignore it if you do not need flink.
export FLINK_PROMETHEUS_HOST="http://localhost:9090"
export FLINK_PROMETHEUS_TOKEN=""
export FLINK_PROMETHEUS_DATABASE=""

# Optional, needed by task-gpt module to get exception solution, ignore if you do not need it.
export CHATGPT_ENABLE=false
# Openai keys needed by enabling chatgpt, random access the key if there are multiple keys.
export CHATGPT_API_KEYS=sk-xxx1,sk-xxx2
# Optional, needed if setting proxy, or keep it empty.
export CHATGPT_PROXY="" # for example, https://proxy.ai
# chatgpt model
export CHATGPT_MODEL="gpt-3.5-turbo"
# chatgpt prompt
export CHATGPT_PROMPT="You are a senior expert in big data, teaching beginners. I will give you some anomalies and you will provide solutions to them."

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

### application-hadoop.yml 说明

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
  
  # task-metadata 模块配置依赖
  yarn:
    - clusterName: "bigdata"
      resourceManager: [ "ip:port" ] # yarn.resourcemanager.webapp.address 属性值
      jobHistoryServer: "ip:port" # mapreduce.jobhistory.webapp.address 属性值
  spark:
    sparkHistoryServer: [ "ip:port" ] # spark history ui 地址
```

更多细节请参考，[模块介绍文档](./deployment_zh_detail.md)
