#!/bin/bash

HOME_DIR=$(cd $(dirname $0)/.. && pwd)
CANAL_VERSION='1.1.6'

if [ ! -f canal.deployer-${CANAL_VERSION}.tar.gz ]; then
  echo 'downloading canal...'
  wget https://github.com/alibaba/canal/releases/download/canal-${CANAL_VERSION}/canal.deployer-${CANAL_VERSION}.tar.gz
fi

if [ -f canal.deployer-${CANAL_VERSION}.tar.gz -a ! -d ${HOME_DIR}/lib ]; then
  tar -zxvf canal.deployer-${CANAL_VERSION}.tar.gz conf -C ${HOME_DIR}
  tar -zxvf canal.deployer-${CANAL_VERSION}.tar.gz lib -C ${HOME_DIR}
  tar -zxvf canal.deployer-${CANAL_VERSION}.tar.gz plugin -C ${HOME_DIR}
fi
