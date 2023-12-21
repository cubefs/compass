#!/bin/bash

service ssh start
ssh-keygen -t rsa -f ~/.ssh/id_rsa -N ""
cp -v ~/.ssh/id_rsa.pub ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys
cd /opt/hadoop
if [ ! -d "/opt/tmp" ]; then
       bin/hdfs namenode -format
fi
sbin/start-dfs.sh
sbin/start-yarn.sh
bin/mapred --daemon start historyserver
bin/hdfs dfsadmin -safemode leave

cd /opt/spark
sbin/start-history-server.sh

tail -f /dev/null
