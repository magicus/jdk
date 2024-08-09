#!/bin/bash

# turn on debugging
#set -xv;

echo "running script_after.sh"

readonly JDK_LIB_PATH="build/jdk/lib/server/libjvm.dylib";

if [ ! -f ${JDK_LIB_PATH} ] ; then
{
    echo ">>>>>>>   Cannot find ${JDK_LIB_PATH}";
    exit 1;
}
fi
