# for mysql
export DATABASE=mysql
export SPRING_PROFILES_ACTIVE=mysql
export SPRING_DATASOURCE_URL="jdbc:mysql://127.0.0.1:3306/dolphinscheduler?useUnicode=true&characterEncoding=UTF-8&useSSL=false"
export SPRING_DATASOURCE_USERNAME=root
export SPRING_DATASOURCE_PASSWORD=Root@666

export REGISTRY_TYPE="zookeeper"
export REGISTRY_ZOOKEEPER_CONNECT_STRING="zookeeper:2181"


export HADOOP_HOME=${HADOOP_HOME:-/opt/hadoop}
export HADOOP_CONF_DIR=${HADOOP_CONF_DIR:-/opt/hadoop/etc/hadoop}
export SPARK_HOME2=${SPARK_HOME:-/opt/spark}
export PYTHON_LAUNCHER=${PYTHON_LAUNCHER:-/opt/python}
export HIVE_HOME=${HIVE_HOME:-/opt/hive}
export FLINK_HOME=${FLINK_HOME:-/opt/flink}
export DATAX_LAUNCHER=${DATAX_LAUNCHER:-/opt/datax/bin/python3}

export PATH=$HADOOP_HOME/bin:$SPARK_HOME/bin:$PYTHON_LAUNCHER:$JAVA_HOME/bin:$HIVE_HOME/bin:$FLINK_HOME/bin:$DATAX_LAUNCHER:$PATH
