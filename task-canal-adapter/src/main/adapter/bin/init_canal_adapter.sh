#!/bin/bash

HOME_DIR=$(cd $(dirname $0)/.. && pwd)
CANAL_ADAPTER_VERSION='1.1.6'

if [ ! -f canal.adapter-${CANAL_ADAPTER_VERSION}.tar.gz ]; then
  echo 'downloading canal adapter...'
  wget https://github.com/alibaba/canal/releases/download/canal-${CANAL_ADAPTER_VERSION}/canal.adapter-${CANAL_ADAPTER_VERSION}.tar.gz
fi

if [ -f canal.adapter-${CANAL_ADAPTER_VERSION}.tar.gz -a ! -d ${HOME_DIR}/lib ]; then
  tar -zxvf canal.adapter-${CANAL_ADAPTER_VERSION}.tar.gz lib -C ${HOME_DIR}
  tar -zxvf canal.adapter-${CANAL_ADAPTER_VERSION}.tar.gz plugin -C ${HOME_DIR}
fi
