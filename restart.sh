#!/bin/sh

# 变量参数化
APP_NS="test"
APP_NAME="qatestplatform"
APP_IMAGE="registry-vpc.cn-hangzhou.aliyuncs.com/muri/$APP_NAME:`date +%Y%m%d%H%M%S`"


# 打包-->做镜像-->上传镜像仓库
mvn clean package -U -Dmaven.test.skip=true
docker build -t $APP_IMAGE -f test_dockerfile .
docker push $APP_IMAGE


# 判断是否为第一次部署
if [ !-n `kubectl get pods -n $APP_NS |grep "^$APP_NAME"` ];then
  # 第一次部署
  sh /usr/local/sbin/k8s.sh $APP_NS $APP_NAME $APP_IMAGE
  kubectl create -f $PWD/$APP_NAME.yaml
else
  echo "开始执行更新操作。。。"
  kubectl -n $APP_NS set image deployment/$APP_NAME $APP_NAME=$APP_IMAGE
fi
echo "启动完成！！！"
sleep 60
