#!/bin/bash

HOME_DIR=$(cd $(dirname $0)/.. && pwd)
ENV_SH=$(dirname $0)/compass_env.sh

# copy compass env
if [ -f ${ENV_SH} ]; then
  source ${ENV_SH}
  for dir in ${HOME_DIR}/task-*; do
    if [ -d $dir ]; then
      cp ${ENV_SH} $dir/bin/
    fi
  done
fi

# copy hadoop conf
cp /${HOME_DIR}/conf/application-hadoop.yml ${HOME_DIR}/task-application/conf
cp /${HOME_DIR}/conf/application-hadoop.yml ${HOME_DIR}/task-metadata/conf
cp /${HOME_DIR}/conf/application-hadoop.yml ${HOME_DIR}/task-parser/conf
cp /${HOME_DIR}/conf/application-hadoop.yml ${HOME_DIR}/task-flink/conf

TASK_CANAL_ENABLE=False
TASK_CANAL_ENABLE=${TASK_CANAL_ENABLE:-"True"}

start() {
  for dir in ${HOME_DIR}/task-*; do
    if [ "$TASK_CANAL_ENABLE" != "True" ] && [[ "$dir" == *task-canal* ]]; then
      echo "Skip $dir"
    elif [ -d $dir ]; then
      cd $dir
      echo $dir
      bash bin/startup.sh
      cd ..
    fi
  done
}

start
