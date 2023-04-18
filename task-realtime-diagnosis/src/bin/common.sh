#!/usr/bin/env bash
PROG_DIR=$(cd $(dirname $0)/.. && pwd)
echo $PROG_DIR
if [ -e $PROG_DIR/../env.sh ];then
    . $PROG_DIR/../env.sh
else
    echo "Does Not exist $PROG_DIR/../env.sh";
fi
