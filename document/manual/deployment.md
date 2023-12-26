# Compass Deployment

[Using Docker Compose To Deploy](../../docker/playground/README.md)

Compass depends on Canal,PostgreSQL(or MySQL),Kafka,Redis,Zookeeper,OpenSearch

## Required Environment
| Dependency | Version | Optional | Description                        |
|------------|---------|----------|------------------------------------|
| Canal      | v1.1.6+ | yes      | needed by Airflow,DolphinScheduler |
| MySQL      | 5.7+    | yes      ||
| PostgreSQL | 10.0+   | no       ||
| Kafka      | all     | no       ||
| Redis      | all     | no       | deployed in cluster mode           |
| Zookeeper  | 3.4.5   | no       | needed by canal                    |
| OpenSearch | 1.3.12  | no       ||

OpenSearch is compatible with Elasticsearch 7.0+.

Compass supports single-machine and cluster deployment, with elastic scalability by module.

## Compile
Use JDK 8 and maven 3.6.0+ to Compile
```
git clone https://github.com/cubefs/compass.git
cd compass
mvn clean package -DskipTests -Pdist
or 
mvn clean package -DskipTests -Pdist,spark   (only pack for spark web ui)
or 
mvn clean package -DskipTests -Pdist,flink   (only pack for flink web ui)
```
Use docker compose to start application
```
cp dist/compass-v1.1.2.tar.gz docker/playground
cd docker/playground/
docker compose --profile dependencies up -d
docker compose --profile compass-demo up -d
```
For more, please see [docker-playground doc](https://github.com/cubefs/compass/blob/main/docker/playground/README.md)

## Initialize database

Support for PostgreSQL(default) or MySQL(you need to manually download [mysql-connector-java](https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.29/mysql-connector-java-8.0.29.jar) and copy it to the "lib" directory of each module, except for the "canal" module) as metadata storage.

table structure consists of two parts: document/sql/compass_.sql, document/sql/dolphinscheduler_.sql (needs to be modified according to the actual used version, supports 2.x and 3.x) or document/sql/airflow_*.sql (supports 2.x).

If you are using a self-developed scheduling platform, please refer to the above sql table structure.

## Configuration

compass/bin and compass/conf are used as public scripts and configurations to facilitate unified startup and configuration management.

```
# Start all modules
./bin/start_all.sh
# Stop all modules
./bin/stop_all.sh
```

### compass_env.sh configuration

Kafka needs to have the topics: mysqldata,task-instance,task-application,exception-log

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

### application-hadoop.yml configuration

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

For more details, please refer to [Module Introduction Document](./deployment_detail.md)
