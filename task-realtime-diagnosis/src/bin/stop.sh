#!/bin/sh
OG_DIR=$(cd $(dirname $0)/.. && pwd)
BINARY="${PROG_DIR}/lib/task-realtime-diagnosis.jar"

ps -ef |grep $BINARY |grep -v grep |awk '{print $2}'|xargs kill -9
