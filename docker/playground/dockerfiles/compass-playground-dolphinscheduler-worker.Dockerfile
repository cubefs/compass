
FROM apache/dolphinscheduler-worker:3.1.8

ARG AWS_JAVA_SDK_VERSION
ARG SPARK_HADOOP_VERSION
ARG POSTGRES_JDBC_VERSION
ARG SCALA_BINARY_VERSION
ARG SPARK_VERSION
ARG SPARK_BINARY_VERSION

ARG APACHE_MIRROR
ARG MAVEN_MIRROR

ENV SPARK_HOME=/opt/soft/spark
ENV SPARK_HOME2=/opt/soft/spark

ENV HADOOP_CONF_DIR=/etc/hadoop/conf
ENV HIVE_CONF_DIR=/etc/hive/conf
ENV SPARK_CONF_DIR=/etc/spark/conf

RUN set -x && \
    mkdir /opt/soft && \
    wget -q ${APACHE_MIRROR}/spark/spark-${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-hadoop3.tgz && \
    tar -xzf spark-${SPARK_VERSION}-bin-hadoop3.tgz -C /opt/soft && \
    ln -s /opt/soft/spark-${SPARK_VERSION}-bin-hadoop3 ${SPARK_HOME} && \
    rm spark-${SPARK_VERSION}-bin-hadoop3.tgz && \
    SPARK_HADOOP_CLOUD_JAR_NAME=spark-hadoop-cloud_${SCALA_BINARY_VERSION} && \
    wget -q ${MAVEN_MIRROR}/org/apache/spark/${SPARK_HADOOP_CLOUD_JAR_NAME}/${SPARK_VERSION}/${SPARK_HADOOP_CLOUD_JAR_NAME}-${SPARK_VERSION}.jar -P ${SPARK_HOME}/jars && \
    HADOOP_CLOUD_STORAGE_JAR_NAME=hadoop-cloud-storage && \
    wget -q ${MAVEN_MIRROR}/org/apache/hadoop/${HADOOP_CLOUD_STORAGE_JAR_NAME}/${SPARK_HADOOP_VERSION}/${HADOOP_CLOUD_STORAGE_JAR_NAME}-${SPARK_HADOOP_VERSION}.jar -P ${SPARK_HOME}/jars && \
    HADOOP_AWS_JAR_NAME=hadoop-aws && \
    wget -q ${MAVEN_MIRROR}/org/apache/hadoop/${HADOOP_AWS_JAR_NAME}/${SPARK_HADOOP_VERSION}/${HADOOP_AWS_JAR_NAME}-${SPARK_HADOOP_VERSION}.jar -P ${SPARK_HOME}/jars && \
    AWS_JAVA_SDK_BUNDLE_JAR_NAME=aws-java-sdk-bundle && \
    wget -q ${MAVEN_MIRROR}/com/amazonaws/${AWS_JAVA_SDK_BUNDLE_JAR_NAME}/${AWS_JAVA_SDK_VERSION}/${AWS_JAVA_SDK_BUNDLE_JAR_NAME}-${AWS_JAVA_SDK_VERSION}.jar -P ${SPARK_HOME}/jars && \
    POSTGRES_JDBC_JAR_NAME=postgresql && \
    wget -q ${MAVEN_MIRROR}/org/postgresql/${POSTGRES_JDBC_JAR_NAME}/${POSTGRES_JDBC_VERSION}/${POSTGRES_JDBC_JAR_NAME}-${POSTGRES_JDBC_VERSION}.jar -P ${SPARK_HOME}/jars \

