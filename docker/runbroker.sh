#!/bin/bash
# 自定义 runbroker.sh - 如果 MAX_HEAP_SIZE 已设置则跳过自动计算
if [ -z "$MAX_HEAP_SIZE" ] ; then
    # 保留原始的 calculate_heap_sizes 逻辑，但设置上限为 768M
    system_memory_in_mb=`free -m | head -2 | tail -1 | awk '{print $2}'`
    half_system_memory_in_mb=`expr $system_memory_in_mb / 2`
    quarter_system_memory_in_mb=`expr $half_system_memory_in_mb / 2`
    
    if [ "$half_system_memory_in_mb" -gt 1024 ]
    then
        half_system_memory_in_mb=1024
    fi
    if [ "$quarter_system_memory_in_mb" -gt 8192 ]
    then
        quarter_system_memory_in_mb=8192
    fi
    if [ "$half_system_memory_in_mb" -gt "$quarter_system_memory_in_mb" ]
    then
        max_heap_size_in_mb="$half_system_memory_in_mb"
    else
        max_heap_size_in_mb="$quarter_system_memory_in_mb"
    fi
    # 强制限制为 768M
    if [ "$max_heap_size_in_mb" -gt 768 ] || [ -z "$max_heap_size_in_mb" ]; then
        max_heap_size_in_mb=768
    fi
    MAX_HEAP_SIZE="${max_heap_size_in_mb}M"
fi

Xms=$MAX_HEAP_SIZE
Xmx=$MAX_HEAP_SIZE
Xmn=${HEAP_NEWSIZE:-200M}
MaxDirectMemorySize=${MaxDirectMemorySize:-$MAX_HEAP_SIZE}

JAVA_OPT="${JAVA_OPT} -server -Xms${Xms} -Xmx${Xmx} -Xmn${Xmn}"
JAVA_OPT="${JAVA_OPT} -XX:+UseG1GC -XX:G1HeapRegionSize=16m -XX:G1ReservePercent=25 -XX:InitiatingHeapOccupancyPercent=30 -XX:+CMSParallelRemarkEnabled -XX:SoftRefLRUPolicyMSPerMB=0 -XX:SurvivorRatio=8 -XX:-UseParNewGC"
JAVA_OPT="${JAVA_OPT} -verbose:gc -Xloggc:/dev/shm/mq_gc_%p.log -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -XX:+PrintAdaptiveSizePolicy"
JAVA_OPT="${JAVA_OPT} -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=5 -XX:GCLogFileSize=30m"
JAVA_OPT="${JAVA_OPT} -XX:-OmitStackTraceInFastThrow"
JAVA_OPT="${JAVA_OPT} -XX:+AlwaysPreTouch"
JAVA_OPT="${JAVA_OPT} -XX:MaxDirectMemorySize=${MaxDirectMemorySize}"
JAVA_OPT="${JAVA_OPT} -XX:-UseLargePages -XX:-UseBiasedLocking"
JAVA_OPT="${JAVA_OPT} -Djava.ext.dirs=${JAVA_HOME}/jre/lib/ext:${BASE_DIR}/lib"
JAVA_OPT="${JAVA_OPT} ${JAVA_OPT_EXT}"
JAVA_OPT="${JAVA_OPT} -cp ${CLASSPATH}"

numactl --interleave=all pwd > /dev/null 2>&1
if [ $? -eq 0 ]
then
    numactl --interleave=all sh ${ROCKETMQ_HOME}/bin/runbroker后面的Java启动命令 $@
else
    sh ${ROCKETMQ_HOME}/bin/runbroker后面的Java启动命令 $@
fi
