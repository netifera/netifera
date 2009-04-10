#! /bin/sh
VM=java
EXE_DIR=`dirname $0`
eval $VM -jar \"$EXE_DIR/startup.jar\" $*
