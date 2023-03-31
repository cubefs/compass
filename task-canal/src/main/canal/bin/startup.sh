#!/bin/bash 
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Modification statement: This script was created by Apache canal, we added the 'ENV_OPTS' variables.

current_path=`pwd`
case "`uname`" in
    Linux)
		bin_abs_path=$(readlink -f $(dirname $0))
		;;
	*)
		bin_abs_path=`cd $(dirname $0); pwd`
		;;
esac
base=${bin_abs_path}/..
canal_conf=$base/conf/canal.properties
canal_local_conf=$base/conf/canal_local.properties
logback_configurationFile=$base/conf/logback.xml

source $bin_abs_path/init_canal.sh

ENV_OPTS=""
if [ -f $bin_abs_path/compass_env.sh ]; then
  source $bin_abs_path/compass_env.sh
  ENV_OPTS="-Dcanal.instance.master.address=$CANAL_INSTANCE_MASTER_ADDRESS \
  -Dcanal.instance.dbUsername=$CANAL_INSTANCE_DBUSERNAME \
  -Dcanal.instance.dbPassword=$CANAL_INSTANCE_DBPASSWORD \
  -Dcanal.instance.filter.regex=$CANAL_INSTANCE_FILTER_REGEX \
  -Dcanal.mq.topic=$CANAL_MQ_TOPIC \
  -Dcanal.serverMode=$CANAL_SERVERMODE \
  -Dkafka.bootstrap.servers=$KAFKA_BOOTSTRAPSERVERS \
  -Dcanal.zkServers=$CANAL_ZKSERVERS"
fi

export LANG=en_US.UTF-8
export BASE=$base

if [ -f $base/bin/canal.pid ] ; then
	echo "found canal.pid , Please run stop.sh first ,then startup.sh" 2>&2
    exit 1
fi

if [ ! -d $base/logs/canal ] ; then 
	mkdir -p $base/logs/canal
fi

## set java path
if [ -z "$JAVA" ] ; then
  JAVA=$(which java)
fi

case "$#" 
in
0 ) 
	;;
1 )	
	var=$*
	if [ "$var" = "local" ]; then
		canal_conf=$canal_local_conf
	else
		if [ -f $var ] ; then 
			canal_conf=$var
		else
			echo "THE PARAMETER IS NOT CORRECT.PLEASE CHECK AGAIN."
			exit
		fi
	fi;;
2 )	
	var=$1
	if [ "$var" = "local" ]; then
		canal_conf=$canal_local_conf
	else
		if [ -f $var ] ; then
			canal_conf=$var
		else 
			if [ "$1" = "debug" ]; then
				DEBUG_PORT=$2
				DEBUG_SUSPEND="n"
				JAVA_DEBUG_OPT="-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=$DEBUG_PORT,server=y,suspend=$DEBUG_SUSPEND"
			fi
		fi
     fi;;
* )
	echo "THE PARAMETERS MUST BE TWO OR LESS.PLEASE CHECK AGAIN."
	exit;;
esac

str=`file -L $JAVA | grep 64-bit`
if [ -n "$str" ]; then
	JAVA_OPTS="-server -Xms2048m -Xmx3072m -Xmn1024m -XX:SurvivorRatio=2 -XX:PermSize=96m -XX:MaxPermSize=256m -Xss256k -XX:-UseAdaptiveSizePolicy -XX:MaxTenuringThreshold=15 -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:+UseCMSCompactAtFullCollection -XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:+HeapDumpOnOutOfMemoryError"
else
	JAVA_OPTS="-server -Xms1024m -Xmx1024m -XX:NewSize=256m -XX:MaxNewSize=256m -XX:MaxPermSize=128m "
fi

JAVA_OPTS=" $JAVA_OPTS -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true -Dfile.encoding=UTF-8"
CANAL_OPTS="-DappName=otter-canal -Dlogback.configurationFile=$logback_configurationFile -Dcanal.conf=$canal_conf"

if [ -e $canal_conf -a -e $logback_configurationFile ]
then 
	
	for i in $base/lib/*;
		do CLASSPATH=$i:"$CLASSPATH";
	done
 	CLASSPATH="$base/conf:$CLASSPATH";
 	
 	echo "cd to $bin_abs_path for workaround relative path"
  	cd $bin_abs_path
 	
	echo LOG CONFIGURATION : $logback_configurationFile
	echo canal conf : $canal_conf 
	echo CLASSPATH :$CLASSPATH
	nohup $JAVA $JAVA_OPTS $JAVA_DEBUG_OPT $CANAL_OPTS $ENV_OPTS -classpath .:$CLASSPATH com.alibaba.otter.canal.deployer.CanalLauncher 1>>$base/logs/canal/canal_stdout.log 2>&1 &
	echo $! > $base/bin/canal.pid 
	
	echo "cd to $current_path for continue"
  	cd $current_path
else 
	echo "canal conf("$canal_conf") OR log configration file($logback_configurationFile) is not exist,please create then first!"
fi
