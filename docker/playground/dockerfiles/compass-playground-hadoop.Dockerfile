
FROM eclipse-temurin:8-focal

RUN set -x && \
    ln -snf /usr/bin/bash /usr/bin/sh && \
    apt-get update -q && \
    apt-get install -yq retry busybox && \
    rm -rf /var/lib/apt/lists/* && \
    mkdir /opt/busybox && \
    busybox --install /opt/busybox

ENV PATH=/opt/java/openjdk/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/busybox

# hadoop

ARG AWS_JAVA_SDK_VERSION
ARG HADOOP_VERSION

ARG APACHE_MIRROR
ARG MAVEN_MIRROR

ENV HADOOP_HOME=/opt/hadoop
ENV HADOOP_CONF_DIR=/etc/hadoop/conf

RUN set -x && \
    if [ $(uname -m) = "aarch64" ]; then HADOOP_TAR_NAME=hadoop-${HADOOP_VERSION}-aarch64; else HADOOP_TAR_NAME=hadoop-${HADOOP_VERSION}; fi && \
    wget -q ${APACHE_MIRROR}/hadoop/core/hadoop-${HADOOP_VERSION}/${HADOOP_TAR_NAME}.tar.gz && \
    tar -xzf ${HADOOP_TAR_NAME}.tar.gz -C /opt && \
    ln -s /opt/hadoop-${HADOOP_VERSION} ${HADOOP_HOME} && \
    rm ${HADOOP_TAR_NAME}.tar.gz && \
    HADOOP_CLOUD_STORAGE_JAR_NAME=hadoop-cloud-storage && \
    wget -q ${MAVEN_MIRROR}/org/apache/hadoop/${HADOOP_CLOUD_STORAGE_JAR_NAME}/${HADOOP_VERSION}/${HADOOP_CLOUD_STORAGE_JAR_NAME}-${HADOOP_VERSION}.jar -P ${HADOOP_HOME}/share/hadoop/hdfs/lib && \
    HADOOP_AWS_JAR_NAME=hadoop-aws && \
    wget -q ${MAVEN_MIRROR}/org/apache/hadoop/${HADOOP_AWS_JAR_NAME}/${HADOOP_VERSION}/${HADOOP_AWS_JAR_NAME}-${HADOOP_VERSION}.jar -P ${HADOOP_HOME}/share/hadoop/hdfs/lib && \
    AWS_JAVA_SDK_BUNDLE_JAR_NAME=aws-java-sdk-bundle && \
    wget -q ${MAVEN_MIRROR}/com/amazonaws/${AWS_JAVA_SDK_BUNDLE_JAR_NAME}/${AWS_JAVA_SDK_VERSION}/${AWS_JAVA_SDK_BUNDLE_JAR_NAME}-${AWS_JAVA_SDK_VERSION}.jar -P ${HADOOP_HOME}/share/hadoop/hdfs/lib

# hive metastore

ARG HIVE_VERSION

ENV HIVE_HOME=/opt/hive
ENV HIVE_CONF_DIR=/etc/hive/conf

RUN set -x && \
    wget -q ${APACHE_MIRROR}/hive/hive-${HIVE_VERSION}/apache-hive-${HIVE_VERSION}-bin.tar.gz && \
    tar -xzf apache-hive-${HIVE_VERSION}-bin.tar.gz -C /opt && \
    ln -s /opt/apache-hive-${HIVE_VERSION}-bin ${HIVE_HOME} && \
    rm apache-hive-${HIVE_VERSION}-bin.tar.gz

ENTRYPOINT ["/opt/hive/bin/hive", "--service", "metastore"]
