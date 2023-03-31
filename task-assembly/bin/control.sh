#!/bin/bash

HOME_DIR=$(cd $(dirname $0)/.. && pwd)
usage="Usage: control.sh  (start|stop) <all|component name> "


if [ $# -le 1 ]; then
  echo $usage
  exit 1
fi

if [ -f $(dirname $0)/compass_env.sh ]; then
  source $(dirname $0)/compass_env.sh
fi

# cp hadoop conf
cp /${HOME_DIR}/conf/application-hadoop.yml ${HOME_DIR}/task-application/conf
cp /${HOME_DIR}/conf/application-hadoop.yml ${HOME_DIR}/task-parser/conf

control_all() {
  for dir in ${HOME_DIR}/task-*; do
    if [ -d $dir ]; then
      cd $dir
      bash bin/control.sh $1
      cd ..
    fi
  done
}

control() {
  case $1 in
  "start")
    if [ "$2" = "all" ]; then
      control_all
    elif [ -d ${HOME_DIR}/$2 ]; then
      cd $dir
      bash bin/control.sh start
      cd ..
    else
      echo "$2 does not exists"
    fi
    ;;
  "stop")
    if [ "$2" = "all" ]; then
      control_all
    elif [ -d ${HOME_DIR}/$2 ]; then
      cd $dir
      bash bin/control.sh stop
      cd ..
    else
      echo "$2 does not exists"
    fi
    ;;
  *)
    echo $usage
    ;;
  esac
}

control $1 $2
