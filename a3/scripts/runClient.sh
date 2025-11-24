#!/bin/bash

export ZKSERVER
export GROUP_NUM
export ZOOCFGDIR
export ZOOCFG

# Ensure exactly one argument was provided
if [ "$#" -ne 1 ]; then
    echo "Error: Invalid number of arguments."
    exit 1
fi

# Ensure the argument is a positive integer
if ! [[ "$1" =~ ^[0-9]+$ ]]; then
    echo "Error: <samples> must be a positive integer."
    exit 1
fi

source $ZOOBINDIR/zkEnv.sh
java -cp "$CLASSPATH:client/bin/main:shared/bin/main:." DistClient {{samples}}
