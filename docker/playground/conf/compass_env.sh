#!/bin/bash

# dolphinscheduler or airflow or custom
export SCHEDULER="dolphinscheduler"
export SPRING_PROFILES_ACTIVE="hadoop,${SCHEDULER}"

# Configuration for Scheduler MySQL, compass will subscribe data from scheduler database via canal
export SCHEDULER_MYSQL_ADDRESS="dolphinscheduler:3306"
export SCHEDULER_MYSQL_DB="dolphinscheduler"
export SCHEDULER_DATASOURCE_URL="jdbc:mysql://${SCHEDULER_MYSQL_ADDRESS}/${SCHEDULER_MYSQL_DB}?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai"
export SCHEDULER_DATASOURCE_USERNAME="root"
export SCHEDULER_DATASOURCE_PASSWORD="Root@666"

# Configuration for compass database(mysql or postgresql)
export DATASOURCE_TYPE="postgresql"
export COMPASS_DATASOURCE_ADDRESS="postgres:5432"
export COMPASS_DATASOURCE_DB="compass"
export SPRING_DATASOURCE_URL="jdbc:postgresql://${COMPASS_DATASOURCE_ADDRESS}/${COMPASS_DATASOURCE_DB}"
export SPRING_DATASOURCE_USERNAME="postgres"
export SPRING_DATASOURCE_PASSWORD="postgres"

# Kafka
export SPRING_KAFKA_BOOTSTRAPSERVERS="kafka:9092"

# Redis
export SPRING_REDIS_CLUSTER_NODES="redis1:6379,redis2:6380,redis3:6381"
# Optional
export SPRING_REDIS_PASSWORD=""

# Zookeeper
export SPRING_ZOOKEEPER_NODES="zookeeper:2181"

# OpenSearch (default version: 1.3.12) or Elasticsearch (7.x~)
export SPRING_OPENSEARCH_NODES="opensearch:9200"
# Optional
export SPRING_OPENSEARCH_USERNAME=""
# Optional
export SPRING_OPENSEARCH_PASSWORD=""
# Optional, needed by OpenSearch, keep empty if OpenSearch does not use truststore.
export SPRING_OPENSEARCH_TRUSTSTORE=""
# Optional, needed by OpenSearch, keep empty if OpenSearch does not use truststore.
export SPRING_OPENSEARCH_TRUSTSTOREPASSWORD=""

# spark.io.compression.codec: lz4/snappy/zstd (default: no compression)
export SPARK_IO_COMPRESSION_CODEC=""

# Prometheus for flink, ignore it if you do not use flink.
# export FLINK_PROMETHEUS_HOST="http://localhost:9090"
# export FLINK_PROMETHEUS_TOKEN=""
# export FLINK_PROMETHEUS_DATABASE=""

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

#-----------------------------------------------------------------------------------
# The following export items will be automatically filled by the configuration above.
#-----------------------------------------------------------------------------------
# task-canal
export CANAL_INSTANCE_MASTER_ADDRESS=${SCHEDULER_MYSQL_ADDRESS}
export CANAL_INSTANCE_DBUSERNAME=${SCHEDULER_DATASOURCE_USERNAME}
export CANAL_INSTANCE_DBPASSWORD=${SCHEDULER_DATASOURCE_PASSWORD}
if [ ${SCHEDULER} == "dolphinscheduler" ]; then
  export CANAL_INSTANCE_FILTER_REGEX="${SCHEDULER_MYSQL_DB}.t_ds_user,${SCHEDULER_MYSQL_DB}.t_ds_project,${SCHEDULER_MYSQL_DB}.t_ds_task_definition,${SCHEDULER_MYSQL_DB}.t_ds_task_instance,${SCHEDULER_MYSQL_DB}.t_ds_process_definition,${SCHEDULER_MYSQL_DB}.t_ds_process_instance,${SCHEDULER_MYSQL_DB}.t_ds_process_task_relation"
elif [ ${SCHEDULER} == "airflow" ]; then
  export CANAL_INSTANCE_FILTER_REGEX="${SCHEDULER_MYSQL_DB}.dag,${SCHEDULER_MYSQL_DB}.serialized_dag,${SCHEDULER_MYSQL_DB}.ab_user,${SCHEDULER_MYSQL_DB}.dag_run,${SCHEDULER_MYSQL_DB}.task_instance"
else
  export CANAL_INSTANCE_FILTER_REGEX=".*\\..*"
fi

export CANAL_ZKSERVERS=${SPRING_ZOOKEEPER_NODES}
export KAFKA_BOOTSTRAPSERVERS=${SPRING_KAFKA_BOOTSTRAPSERVERS}
export CANAL_MQ_TOPIC="mysqldata"
export CANAL_SERVERMODE="kafka"

# task-canal-adapter
export CANAL_ADAPTER_KAFKA_BOOTSTRAP_SERVERS=${SPRING_KAFKA_BOOTSTRAPSERVERS}
# source datasource
export CANAL_ADAPTER_SOURCE_DATASOURCE_URL=${SCHEDULER_DATASOURCE_URL}
export CANAL_ADAPTER_SOURCE_DATASOURCE_USERNAME=${SCHEDULER_DATASOURCE_USERNAME}
export CANAL_ADAPTER_SOURCE_DATASOURCE_PASSWORD=${SCHEDULER_DATASOURCE_PASSWORD}
# destination datasource
export CANAL_ADAPTER_DESTINATION_DATASOURCE_URL=${SPRING_DATASOURCE_URL}
export CANAL_ADAPTER_DESTINATION_DATASOURCE_USERNAME=${SPRING_DATASOURCE_USERNAME}
export CANAL_ADAPTER_DESTINATION_DATASOURCE_PASSWORD=${SPRING_DATASOURCE_PASSWORD}

# task-syncer
# source mysql
export SPRING_DATASOURCE_DYNAMIC_DATASOURCE_SOURCE_URL=${SCHEDULER_DATASOURCE_URL}
export SPRING_DATASOURCE_DYNAMIC_DATASOURCE_SOURCE_USERNAME=${SCHEDULER_DATASOURCE_USERNAME}
export SPRING_DATASOURCE_DYNAMIC_DATASOURCE_SOURCE_PASSWORD=${SCHEDULER_DATASOURCE_PASSWORD}
# destination mysql
export SPRING_DATASOURCE_DYNAMIC_DATASOURCE_DIAGNOSE_URL=${SPRING_DATASOURCE_URL}
export SPRING_DATASOURCE_DYNAMIC_DATASOURCE_DIAGNOSE_USERNAME=${SPRING_DATASOURCE_USERNAME}
export SPRING_DATASOURCE_DYNAMIC_DATASOURCE_DIAGNOSE_PASSWORD=${SPRING_DATASOURCE_PASSWORD}

