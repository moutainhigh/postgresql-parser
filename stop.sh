#!/bin/bash
pid=`jps | grep 'postgresql-parser-1.0-SNAPSHOT' | grep -v grep | awk '{print $1}'`

if [ -z "$pid" ]; then
  echo "No postgresql-parser server to stop"
  exit 1
else
  kill -s TERM $pid
fi


