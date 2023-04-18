#!/bin/sh
rm -f tpid
#source $(dirname $0)/common.sh
PROG_DIR=$(cd $(dirname $0)/.. && pwd)
APP_NAME="task-realtime-diagnosis"
BINARY="${PROG_DIR}/lib/${APP_NAME}.jar"

if [ "$1" = "debug" ]; then
        DEBUG_PORT=$2
        DEBUG_SUSPEND="n"
        JAVA_DEBUG_OPT="-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=$DEBUG_PORT,server=y,suspend=$DEBUG_SUSPEND"
fi

__pids_pidof() {
    pidof -c -m -o $$ -o $PPID -x "${BINARY}"
}

# shellcheck disable=SC2112
function check(){
    local pid
    # shellcheck disable=SC2039
    pid=$(cat tpid)
    echo $pid
    if [ ! -n "$pid" ];then
        echo "[check] ${BINARY} is already stoped"
        return 1
    else
        echo "[check] ${BINARY} is running, pid is $pid"
        return 0
    fi
}

JAVA_OPTS="-DappName=${APP_NAME} -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true -Dfile.encoding=UTF-8 -Djava.security.egd=file:/dev/./urandom"
JAVA_OPTS_MEM="-server -Xms2g -Xmx2g -XX:NewSize=1g -XX:MaxNewSize=1g -XX:PermSize=128m -XX:MaxPermSize=128m"
JAVA_OPTS_CMS="-XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=75 -XX:+UseCMSInitiatingOccupancyOnly"
JAVA_OPTS_GC="-XX:+PrintTenuringDistribution -XX:+PrintGCDateStamps -XX:+PrintGCDetails -Xloggc:logs/gc-${APP_NAME}.log"

nohup java $JAVA_OPTS $JAVA_OPTS_MEM $JAVA_OPTS_CMS $JAVA_OPTS_GC $JAVA_DEBUG_OPT -jar -Dspring.config.location=$PROG_DIR/../conf/application.yml $BINARY &

echo $! > tpid

check

echo Start Success!