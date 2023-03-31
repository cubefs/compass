#!/bin/bash

HOME_DIR=$(cd $(dirname $0)/.. && pwd)

stop() {
  for dir in ${HOME_DIR}/task-*; do
    if [ -d $dir ]; then
      cd $dir
      echo $dir
      bash bin/stop.sh
      cd ..
    fi
  done
}

stop
