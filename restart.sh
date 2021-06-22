#!/bin/sh

cd /home/dev/workspace/autoTest_pipeline_Test
echo "开始打包编译代码"
mvn clean install -Dmaven.test.skip=true
echo "开始重启服务"
nohup java -jar /home/dev/workspace/autoTest_pipeline_Test/meprint-system/target/meprint-system-2.6.jar > /home/dev/logs/meprint.log 2>&1 &
echo "启动完成！！！"
