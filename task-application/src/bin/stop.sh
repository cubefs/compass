#!/bin/bash

HOME_DIR=$(cd $(dirname $0)/.. && pwd)
APP_NAME="task-application"
PID_FILE=${HOME_DIR}/tpid

stop() {
  if [ -f ${PID_FILE} ]; then
    local pid=$(cat ${PID_FILE})
    echo $APP_NAME $pid
    command="ps --pid ${pid}"
    if [[ $(uname) == "Darwin" ]]; then
      command="ps -p ${pid}"
    fi
    if eval ${command} >/dev/null; then
      kill $pid && rm -f $PID_FILE
    fi
    sleep 5

    if eval ${command} >/dev/null; then
      kill -9 $pid
    fi

  else
    ps -ef | grep $APP_NAME | grep -v grep | awk '{print $2}' | xargs kill -9
  fi
  rm -f $PID_FILE
}

stop