#!/bin/bash
source /etc/profile
if [ -f /root/.bashrc ]; then
    source /root/.bashrc
else
        if [ -f ~/.bashrc ]; then
                source ~/.bashrc
        fi
fi

#初始化基础参数
CUR_DIR=$(cd "$(dirname $0)"; pwd)

jar_name="postgresql-parser-1.0-SNAPSHOT.jar"
jar_file="${CUR_DIR}/${jar_name}"
log_dir="${CUR_DIR}/nohup.out"
lib_path="${CUR_DIR}/lib"

if [ `jps | grep 'postgresql-parser-1.0-SNAPSHOT' | grep -v grep | awk '{print $1}' | wc -l` -eq 0 ];then
        nohup ${JAVA_HOME}/bin/java -Xmx8g -Xms8g -XX:NewRatio=1 -XX:SurvivorRatio=2 -Djava.ext.dirs=${lib_path}  -jar ${jar_file} 2>&1 &
else
        echo 'postgresql-parser has started already!'
fi

