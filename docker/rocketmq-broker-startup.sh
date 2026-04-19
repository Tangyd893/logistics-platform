#!/bin/bash
# 直接设置 MAX_HEAP_SIZE 和 HEAP_NEWSIZE，绕过 runbroker.sh 的自动计算
export MAX_HEAP_SIZE=768M
export HEAP_NEWSIZE=200M
export MaxDirectMemorySize=768M
exec /bin/bash /home/rocketmq/rocketmq-5.3.0/bin/runbroker.sh -Drmq.logback.configurationFile=/home/rocketmq/rocketmq-5.3.0/conf/rmq.broker.logback.xml org.apache.rocketmq.broker.BrokerStartup "$@"
