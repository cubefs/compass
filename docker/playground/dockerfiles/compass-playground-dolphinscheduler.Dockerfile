FROM eclipse-temurin:8-focal

RUN set -x && \
    ln -snf /usr/bin/bash /usr/bin/sh && \
    apt-get update -q && \
    apt-get install -y tzdata &&\
    apt-get install -yq retry busybox && \
    apt-get install -y openssh-client openssh-server && \
    apt-get install -y sudo && \
    apt-get install -yq mysql-server && \
    rm -rf /var/lib/apt/lists/* && \
    mkdir /opt/busybox && \
    busybox --install /opt/busybox


ENV PATH=/opt/java/openjdk/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/busybox

# Python
RUN set -x && \
    apt-get update -q && \
    apt-get install -y software-properties-common && \
    add-apt-repository ppa:deadsnakes/ppa -y && \
    apt-get install -yq install python3.8 && \
    apt-get install -yq python3-pip

ARG MYSQL_DATABASE
ARG MYSQL_USERNAME
ARG MYSQL_ROOT_PASSWORD
ARG TZ
ARG HADOOP_VERSION
ARG SPARK_VERSION

ENV MYSQL_DATABASE=$MYSQL_DATABASE
ENV MYSQL_USER=$MYSQL_USER
ENV MYSQL_PASSWORD=$MYSQL_PASSWORD
ENV MYSQL_ROOT_PASSWORD=$MYSQL_ROOT_PASSWORD


COPY apache-dolphinscheduler-3.1.5-bin.tar.gz /opt

RUN set -x && \
    tar -xzf /opt/apache-dolphinscheduler-3.1.5-bin.tar.gz -C /opt && \
    ln -s /opt/apache-dolphinscheduler-3.1.5-bin /opt/ds && \
    rm /opt/apache-dolphinscheduler-3.1.5-bin.tar.gz
    

COPY mysql-connector-java-8.0.19.jar /opt/ds/tools/libs
COPY mysql-connector-java-8.0.19.jar /opt/ds/api-server/libs
COPY mysql-connector-java-8.0.19.jar /opt/ds/master-server/libs
COPY mysql-connector-java-8.0.19.jar /opt/ds/worker-server/libs

COPY conf/ds/install_env.sh /opt/ds/bin/env
COPY conf/ds/dolphinscheduler_env.sh /opt/ds/bin/env
COPY conf/ds/ds-entrypoint.sh /opt/ds
COPY conf/ds/common.properties /opt/ds/api-server/conf
COPY conf/ds/common.properties /opt/ds/worker-server/conf


# MySQL
RUN set -ex && \
    chmod 755 /opt/ds/ds-entrypoint.sh && \
    sed -i '/\[mysqld\]/a server-id=1' /etc/mysql/mysql.conf.d/mysqld.cnf && \
    sed -i '/\[mysqld\]/a log-bin=mysql-bin' /etc/mysql/mysql.conf.d/mysqld.cnf && \
    sed -i '/\[mysqld\]/a binlog_format=row' /etc/mysql/mysql.conf.d/mysqld.cnf && \
    sed -i "s/^bind-address.*/bind-address = 0.0.0.0/" /etc/mysql/mysql.conf.d/mysqld.cnf && \
    service mysql start && \
    mysql -uroot -e "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'Root@666'" && \
    mysql -uroot -pRoot@666 -e "CREATE DATABASE dolphinscheduler DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci" && \
    mysql -uroot -pRoot@666 -e "use mysql; update user set host='%' where user='root'; GRANT ALL ON *.* TO 'root'@'%'; GRANT ALL ON *.* TO 'root'@'%'; flush privileges;" || true


ENV HADOOP_TAR_NAME=hadoop-${HADOOP_VERSION}.tar.gz
COPY ${HADOOP_TAR_NAME} /opt
COPY spark-${SPARK_VERSION}-bin-hadoop3.tgz /opt
COPY apache-flume-1.11.0-bin.tar.gz /opt

# Hadoop, Spark, Flume
RUN set -x && \
    tar -xzf /opt/hadoop-${HADOOP_VERSION}.tar.gz -C /opt && \
    ln -s /opt/soft/hadoop-${HADOOP_VERSION} /opt/hadoop && \
    rm /opt/hadoop-${HADOOP_VERSION}.tar.gz && \
    tar -xzf /opt/spark-${SPARK_VERSION}-bin-hadoop3.tgz -C /opt && \
    ln -s /opt/spark-${SPARK_VERSION}-bin-hadoop3 /opt/spark && \
    rm /opt/spark-${SPARK_VERSION}-bin-hadoop3.tgz && \
    tar -xzf /opt/apache-flume-1.11.0-bin.tar.gz -C /opt && \
    ln -s /opt/apache-flume-1.11.0-bin /opt/flume && \
    rm /opt/apache-flume-1.11.0-bin.tar.gz

COPY script/ds2hdfs.py /opt/soft

COPY conf/hadoop/core-site.xml /opt/soft/hadoop/etc/hadoop
COPY conf/hadoop/hdfs-site.xml /opt/soft/hadoop/etc/hadoop
COPY conf/hadoop/mapred-site.xml /opt/soft/hadoop/etc/hadoop
COPY conf/hadoop/yarn-site.xml /opt/soft/hadoop/etc/hadoop
COPY conf/hadoop/hadoop-env.sh /opt/soft/hadoop/etc/hadoop


ENTRYPOINT ["/opt/ds/ds-entrypoint.sh"]
