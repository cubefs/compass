#!/bin/bash

HOME_DIR=$(cd $(dirname $0)/.. && pwd)
APP_NAME="task-gpt"
PID_FILE=${HOME_DIR}/tpid

if [ -f $(dirname $0)/compass_env.sh ]; then
  source $(dirname $0)/compass_env.sh
fi

check() {
  if [ -f ${PID_FILE} ]; then
    local pid=$(cat ${PID_FILE})
    echo $pid
    command="ps --pid ${pid}"
    if [[ $(uname) == "Darwin" ]]; then
      command="ps -p ${pid}"
    fi
    if eval ${command} >/dev/null; then
      echo "${APP_NAME} is running, pid=${pid}. Please stop first!"
      exit 1
    fi
  fi
}

start() {
  check

  JAVA_OPTS="-DappName=${APP_NAME} -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true -Dfile.encoding=UTF-8 -Djava.security.egd=file:/dev/./urandom"
  JAVA_OPTS_GC="-server -XX:+UseG1GC -XX:G1HeapRegionSize=8m -verbose:GC -XX:+PrintGCDateStamps -XX:+PrintGCDetails -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${HOME_DIR}/logs/dump.hprof -Xloggc:${HOME_DIR}/logs/gc-${APP_NAME}.log"

  nohup java $JAVA_OPTS $JAVA_OPTS_GC -cp "${HOME_DIR}/conf":"${HOME_DIR}/lib/*" com.oppo.cloud.gpt.TaskGptApplication >/dev/null 2>&1 &

  pid=$!
  echo $pid
  echo $pid >$PID_FILE
}

start
