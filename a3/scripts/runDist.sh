#!/bin/bash

export ZKSERVER
export GROUP_NUM

. $ZOOBINDIR/zkEnv.sh
java -cp "$CLASSPATH:dist/bin/main:shared/bin/main:." DistProcess
