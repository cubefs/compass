#!/bin/bash

service mysql start

cd /opt/dolphinscheduler
useradd -ms /bin/bash dolphinscheduler && echo 'dolphinscheduler:dolphinscheduler' | chpasswd
sed -i '$adolphinscheduler  ALL=(ALL)  NOPASSWD: NOPASSWD: ALL' /etc/sudoers
sed -i 's/Defaults    requirett/#Defaults    requirett/g' /etc/sudoers
bash ./tools/bin/upgrade-schema.sh
mysql -uroot -pRoot@666 -e "insert into dolphinscheduler.t_ds_tenant(id,tenant_code,queue_id) values(1,'root',1);update dolphinscheduler.t_ds_user set tenant_id=1 where id=1;"
bash ./bin/dolphinscheduler-daemon.sh start master-server
bash ./bin/dolphinscheduler-daemon.sh start worker-server
bash ./bin/dolphinscheduler-daemon.sh start api-server


cd  /opt
export HADOOP_HOME=/opt/hadoop
python3.8 -m pip install watchdog
nohup python3.8 ./ds2hdfs.py > ./ds2hdfs.log 2>&1 &


tail -f /dev/null
