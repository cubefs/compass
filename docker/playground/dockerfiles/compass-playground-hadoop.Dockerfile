FROM eclipse-temurin:8-focal

RUN set -x && \
    ln -snf /usr/bin/bash /usr/bin/sh && \
    apt-get update -q && \
    apt-get install -y tzdata &&\
    apt-get install -yq retry busybox && \
    apt-get install -y openssh-client openssh-server && \
    rm -rf /var/lib/apt/lists/* && \
    mkdir /opt/busybox && \
    busybox --install /opt/busybox

ENV PATH=/opt/java/openjdk/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/busybox

# hadoop
ARG HADOOP_VERSION

ARG APACHE_MIRROR
ARG MAVEN_MIRROR

ENV HADOOP_HOME=/opt/hadoop
ENV HADOOP_TAR_NAME=hadoop-${HADOOP_VERSION}.tar.gz
COPY ${HADOOP_TAR_NAME} /opt

RUN set -x && \
    tar -xzf /opt/hadoop-${HADOOP_VERSION}.tar.gz -C /opt && \
    ln -s /opt/hadoop-${HADOOP_VERSION} ${HADOOP_HOME} && \
    rm /opt/hadoop-${HADOOP_VERSION}.tar.gz

COPY conf/hadoop/core-site.xml /opt/hadoop-${HADOOP_VERSION}/etc/hadoop
COPY conf/hadoop/hdfs-site.xml /opt/hadoop-${HADOOP_VERSION}/etc/hadoop
COPY conf/hadoop/mapred-site.xml /opt/hadoop-${HADOOP_VERSION}/etc/hadoop
COPY conf/hadoop/yarn-site.xml /opt/hadoop-${HADOOP_VERSION}/etc/hadoop
COPY conf/hadoop/hadoop-env.sh /opt/hadoop-${HADOOP_VERSION}/etc/hadoop
COPY conf/hadoop/hadoop-entrypoint.sh ${HADOOP_HOME}


RUN set -ex && \
    chmod 755 ${HADOOP_HOME}/hadoop-entrypoint.sh


ENTRYPOINT ["/opt/hadoop/hadoop-entrypoint.sh"]
