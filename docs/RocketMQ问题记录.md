# RocketMQ 5.x 问题记录与解决方案

> 本文档记录 RocketMQ 5.x 在本项目中的已知问题、原因分析及解决方案。

---

## 问题一： Broker 默认端口 8080 与 Spring Boot 冲突

### 现象

Broker 启动后尝试监听 8080 端口，与 Spring Boot 后端（Tomcat 默认 8080）产生端口冲突：

```
java.net.BindException: 地址已在使用
  at java.base/sun.nio.ch.Net.bind0(Native Method)
  ...
org.apache.tomcat.util.net.AbstractEndpoint.start: Unable to start embedded Tomcat
```

### 原因

RocketMQ 5.x Broker 默认 remoting 端口为 **8080**，与 Spring Boot 默认端口相同。

### 解决方案

在 `broker.conf` 中指定 `listenPort`:

```bash
# docker/rocketmq_data/broker/broker.conf
brokerClusterName = DefaultCluster
brokerName = broker-a
brokerId = 0
deleteWhen = 04
fileReservedTime = 48
brokerRole = ASYNC_MASTER
flushDiskType = ASYNC_FLUSH
listenPort = 10811   # ← 改为非冲突端口
```

docker-compose 中使用**相对路径**挂载配置文件：

```yaml
rocketmq-broker:
  volumes:
    - ./rocketmq_data/broker:/home/rocketmq/rocketmq-5.3.0/broker/logs
  command: sh mqbroker -n rocketmq-namesrv:9876 -c /home/rocketmq/rocketmq-5.3.0/broker/logs/broker.conf
  # 注意：broker.conf 在容器内的路径
```

> ⚠️ 容器内 `broker/logs/` 目录即 `broker.conf` 所在位置（挂载的宿主机目录）

---

## 问题二： JAVA_OPT 环境变量不生效，Broker 堆内存远超预期

### 现象

Broker 启动后实际使用 `-Xms1973M -Xmx1973M`，远超过 docker-compose 中配置的 768m/1536m：

```
/opt/java/openjdk/bin/java -Xms768m -Xmx768m -server -Xms1973M -Xmx1973M ...
                                                   ↑后面的参数覆盖前面的
```

### 原因分析

RocketMQ 5.x 的 `runbroker.sh` 脚本逻辑：

```bash
# runbroker.sh 中
JAVA_OPT="${JAVA_OPT} -server -Xms${Xms} -Xmx${Xmx} -Xmn${Xmn}"
# Xms/Xmx/Xmn 从 MAX_HEAP_SIZE 环境变量计算得出
# 宿主机 8G → quarter_system_memory ≈ 1973M
# JAVA_OPT 环境变量被追加而不是覆盖，最终命令行出现两套参数
```

关键代码（来自容器内 `/home/rocketmq/rocketmq-5.3.0/bin/runbroker.sh`）：

```bash
# 第1步：自动计算堆大小（无条件）
calculate_heap_sizes() {
    system_memory_in_mb=`free -m | head -2 | tail -1 | awk '{print $2}'`
    half_system_memory_in_mb=`expr $system_memory_in_mb / 2`
    quarter_system_memory_in_mb=`expr $half_system_memory_in_mb / 2`
    # quarter_system_memory_in_mb = 8192/4 = 2048 → 但最大上限8192/4=2048，实际取 quarter
    max_heap_size_in_mb="$quarter_system_memory_in_mb"   # 1973M (8G系统)
    MAX_HEAP_SIZE="${max_heap_size_in_mb}M"
    HEAP_NEWSIZE="${desired_yg_in_mb}M"   # = max_heap_size/4 ≈ 493M
}

# 第2步：构建 JAVA_OPT
JAVA_OPT="${JAVA_OPT} -server -Xms${Xms} -Xmx${Xmx} -Xmn${Xmn}"
# Xms=$MAX_HEAP_SIZE → "1973M"

# 第3步：如果 docker-compose 设置了 JAVA_OPT="-Xms768m"，则追加
# JAVA_OPT="-Xms768m -Xms1973M -Xmx1973M"  (追加模式)
# JVM 命令行最后一个 -Xms/-Xmx 生效 → 1973M
```

### 尝试过的失败方案

| 方案 | 失败原因 |
|------|---------|
| `JAVA_OPT="-Xms768m -Xmx768m"` | runbroker.sh 追加参数，JVM 取最后一个 |
| `MAX_HEAP_SIZE=768M` | `calculate_heap_sizes()` 无条件覆盖 |
| 自定义 startup.sh 覆盖 `JAVA_OPT` | runbroker.sh 追加逻辑无法绕过 |

### 临时解决方案（当前采用）

将 docker-compose 中 Broker 的 `mem_limit` 提高到 **1536m**（大于默认 1973M 的最大堆），同时 Broker 实际使用 ~1.2GB。

```yaml
rocketmq-broker:
  mem_limit: 1536m
  environment:
    JAVA_OPT: "-Xms1536m -Xmx1536m"   # 仍被追加，但物理内存足够
```

### 根本解决方案（待实现）

修改 `/home/rocketmq/rocketmq-5.3.0/bin/runbroker.sh`，在 `calculate_heap_sizes()` 开头检查 `MAX_HEAP_SIZE` 是否已设置：

```bash
calculate_heap_sizes() {
    # 如果 MAX_HEAP_SIZE 已设置（外部传入），跳过自动计算
    if [ -n "$MAX_HEAP_SIZE" ]; then
        return
    fi
    # ... 原有的自动计算逻辑 ...
}
```

需要通过 volume 挂载自定义 `runbroker.sh` 覆盖容器内脚本。

---

## 问题三： `rocketmq.name-server` 属性名错误

### 现象

Spring Boot 启动时警告：
```
The necessary spring property 'rocketmq.name-server' is not defined, all rockertmq beans creation are skipped!
```

### 原因

`application.yml` 中使用了错误的属性名。

### 解决方案

```yaml
# 错误
rocketmq:
  namesrv-addr: localhost:9876   # ❌ 错误

# 正确（Spring Boot 3.x RocketMQ starter）
rocketmq:
  name-server: localhost:9876    # ✅ 正确
```

---

## 问题四： RocketMQ 5.x 启动脚本 `JAVA_OPT` vs `JAVA_OPT_`

RocketMQ 5.x 的 `runbroker.sh` 使用 `JAVA_OPT` 环境变量（单数）设置 JVM 参数。

| 环境变量 | 用途 |
|---------|------|
| `JAVA_OPT` | JVM 启动参数（如 `-Xms768m -Xmx768m`） |
| `JAVA_OPT_EXT` | 扩展参数（会被追加到 JAVA_OPT 之后） |
| `XMS`/`XMX`/`XMN` | 直接控制堆大小，由 `calculate_heap_sizes()` 计算 |

> RocketMQ 4.x 使用 `JAVA_OPTS`（复数），5.x 已改为 `JAVA_OPT`。

---

## 快速参考

| 项目 | 值 |
|------|---|
| NameServer 端口 | 9876 |
| Broker remoting 端口 | **10811**（默认8080已修改） |
| Broker ha 端口 | 10911 |
| Broker 容器内存限制 | 1536m |
| NameServer 容器内存限制 | 200m |
| docker-compose 文件 | `docker/docker-compose.yml` |
| broker.conf 位置 | `docker/rocketmq_data/broker/broker.conf` |
| 容器内 broker.conf 路径 | `/home/rocketmq/rocketmq-5.3.0/broker/logs/broker.conf` |
| 宿主机 broker.conf 路径 | `docker/rocketmq_data/broker/broker.conf`（挂载） |

## 日志查看

```bash
# Broker 日志
docker logs logistics_rocketmq_broker --tail 50 -f

# NameServer 日志
docker logs logistics_rocketmq_namesrv --tail 50 -f

# 内存状态
docker stats --no-stream logistics_rocketmq_broker logistics_rocketmq_namesrv
```
