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
cp /${HOME_DIR}/conf/application-hadoop.yml ${HOME_DIR}/task-realtime-diagnosis/conf

start() {
  for dir in ${HOME_DIR}/task-*; do
    if [ -d $dir ]; then
      cd $dir
      echo $dir
      bash bin/startup.sh
      cd ..
    fi
  done
}

start
