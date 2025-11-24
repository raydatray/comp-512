#!/bin/bash

export ZKSERVER
export GROUP_NUM
export ZOOCFGDIR=""
export ZOOCFG=""

. $ZOOBINDIR/zkEnv.sh
java -cp "$CLASSPATH:dist/bin/main:shared/bin/main:." DistProcess
