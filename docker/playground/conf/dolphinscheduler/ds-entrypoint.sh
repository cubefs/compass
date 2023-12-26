#!/bin/bash

service mysql start

cd /opt/dolphinscheduler
useradd -ms /bin/bash dolphinscheduler && echo 'dolphinscheduler:dolphinscheduler' | chpasswd
sed -i '$adolphinscheduler  ALL=(ALL)  NOPASSWD: NOPASSWD: ALL' /etc/sudoers
sed -i 's/Defaults    requirett/#Defaults    requirett/g' /etc/sudoers
bash ./tools/bin/upgrade-schema.sh
bash ./bin/dolphinscheduler-daemon.sh start master-server
bash ./bin/dolphinscheduler-daemon.sh start worker-server
bash ./bin/dolphinscheduler-daemon.sh start api-server


cd  /opt
export HADOOP_HOME=/opt/hadoop
python3.8 -m pip install watchdog
nohup python3.8 ./ds2hdfs.py > ./ds2hdfs.log 2>&1 &


tail -f /dev/null
