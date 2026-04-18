# RocketMQ 在综合物流平台中的职责与工作流程

> 本文档介绍 RocketMQ 5.x 在本项目中的角色定位、消息流转设计、以及实际配置使用方法。

---

## 一、为什么需要 RocketMQ

综合物流平台有三条核心业务主线：

```
订单管理 ──→ 仓储作业 ──→ 运输配送
```

每个环节完成后的动作是异步的：
- 订单「已支付」→ 需要自动创建入库单、通知仓库备货
- 仓库「已出库」→ 需要自动创建运单、通知司机取货
- 运输「已签收」→ 需要通知客户、更新订单为已完成

如果用同步调用：
- 耦合严重：订单服务要知道仓库服务的地址
- 性能差：一个环节卡住全流程卡住
- 扩展差：新加环节要改所有旧代码

RocketMQ 解决了这些问题：**发送方只管发，接收方只管收，两边完全解耦。**

---

## 二、系统中的消息生产者

### 2.1 订单状态变更 → 触发仓库/运输事件

当订单状态发生变化时，`OrderService` 通过 `RocketMQTemplate` 发送消息：

```java
// 订单服务 - OrderService.java

@Autowired
private RocketMQTemplate rocketMQTemplate;

public void updateStatus(Long orderId, Integer newStatus, String remark) {
    // 1. 更新订单状态
    Order order = orderRepository.findById(orderId);
    Integer oldStatus = order.getStatus();
    order.setStatus(newStatus);
    orderRepository.save(order);

    // 2. 记录状态日志
    orderStatusLogRepository.save(...);

    // 3. 发送状态变更消息（异步）
    if (shouldNotifyWarehouse(newStatus)) {
        rocketMQTemplate.convertAndSend("logistics:order:warehouse",
            new OrderWarehouseEvent(orderId, order.getOrderNo(), newStatus));
    }
    if (shouldNotifyTransport(newStatus)) {
        rocketMQTemplate.convertAndSend("logistics:order:transport",
            new OrderTransportEvent(orderId, order.getOrderNo(), newStatus));
    }
}
```

### 2.2 状态触发规则

| 订单状态 | 触发动作 |
|---------|---------|
| 20（已确认） | 通知仓库准备拣货 |
| 40（已出库） | 创建运单，通知司机取货 |
| 80（已完成） | 更新运输单状态 |
| 90（已取消） | 通知仓库/运输取消 |

---

## 三、消费者：仓库服务

```java
@RocketMQMessageListener(
    topic = "logistics:order:warehouse",
    consumerGroup = "warehouse-consumer-group"
)
public class OrderWarehouseListener implements RocketMQListener<OrderWarehouseEvent> {

    @Override
    public void onMessage(OrderWarehouseEvent event) {
        // 收到订单已确认的消息 → 创建入库单
        if (event.getNewStatus() == 20) {
            inboundOrderService.createFromOrder(event.getOrderId());
        }
        // 收到订单取消的消息 → 作废入库单
        if (event.getNewStatus() == 90) {
            inboundOrderService.cancelByOrderId(event.getOrderId());
        }
    }
}
```

---

## 四、消费者：运输服务

```java
@RocketMQMessageListener(
    topic = "logistics:order:transport",
    consumerGroup = "transport-consumer-group"
)
public class OrderTransportListener implements RocketMQListener<OrderTransportEvent> {

    @Override
    public void onMessage(OrderTransportEvent event) {
        // 收到订单已出库的消息 → 创建运单
        if (event.getNewStatus() == 40) {
            waybillService.createFromOrder(event.getOrderId());
        }
    }
}
```

---

## 五、消息 Topic 设计

```
logistics:order:warehouse    # 订单 → 仓库
logistics:order:transport     # 订单 → 运输
logistics:transport:status   # 运输状态变更（预留）
logistics:warehouse:stock     # 库存变更（预留）
```

命名规范：`系统:来源:目标`

---

## 六、项目中的实际配置

### 6.1 Docker Compose 中的配置

```yaml
rocketmq-namesrv:
  image: apache/rocketmq:5.3.0
  ports:
    - "9876:9876"          # NameServer 注册端口
  mem_limit: 200m
  environment:
    JAVA_OPT: "-Xms200m -Xmx200m"

rocketmq-broker:
  image: apache/rocketmq:5.3.0
  ports:
    - "10911:10911"        # Broker remoting 端口（内部通信）
    # 注意：remoting 端口已改为 10811（通过 broker.conf）
  mem_limit: 1536m
  environment:
    JAVA_OPT: "-Xms1536m -Xmx1536m"
  volumes:
    - ./rocketmq_data/broker:/home/rocketmq/rocketmq-5.3.0/broker/logs
  command: sh mqbroker -n rocketmq-namesrv:9876 -c /home/rocketmq/rocketmq-5.3.0/broker/logs/broker.conf
```

### 6.2 Broker 配置文件

`docker/rocketmq_data/broker/broker.conf`：

```properties
brokerClusterName = DefaultCluster
brokerName = broker-a
brokerId = 0
deleteWhen = 04
fileReservedTime = 48
brokerRole = ASYNC_MASTER
flushDiskType = ASYNC_FLUSH
listenPort = 10811   # remoting 端口（原默认8080被后端占用）
```

### 6.3 Spring Boot 中的配置

`backend/src/main/resources/application.yml`：

```yaml
rocketmq:
  name-server: ${ROCKETMQ_NAMESRV_ADDR:localhost:9876}
```

### 6.4 Spring Boot 中的使用

```java
@Autowired
private RocketMQTemplate rocketMQTemplate;

// 发送消息
rocketMQTemplate.convertAndSend("logistics:order:warehouse", event);

// 同步发送（带返回值）
T result = rocketMQTemplate.sendAndReceive("logistics:topic", message, T.class, 3000);
```

---

## 七、消息可靠性保证

### 7.1 发送端

- **同步发送 + 确认**：`rocketMQTemplate.send()` 默认同步，会等待 Broker 确认
- **事务消息**（可选）：先写本地事务，再发消息，Broker 回查确认后才真正投递

### 7.2 Broker 端

- **主从同步**：ASYNC_MASTER 模式，写入主节点后异步同步到从节点
- **持久化**：ASYNC_FLUSH，异步刷盘，性能优先

### 7.3 消费端

- **集群消费模式**：多个消费者组成消费组，一条消息只被组内一个实例消费
- **手动 ACK**（可选）：消息处理完成后手动确认

---

## 八、消息流转全流程图

```
用户创建订单
     │
     ▼
POST /api/order/orders
     │
     ▼
OrderService.create()
     │
     ├─→ 保存订单到 PostgreSQL
     │
     ├─→ 发送 RocketMQ 消息
     │   Topic: logistics:order:warehouse
     │   Message: OrderWarehouseEvent(orderId=xxx, status=20)
     │
     ▼
订单状态 = 已确认(20)
     │
     ▼
[Warehouse Consumer Group]
     │
     ├─→ 监听 logistics:order:warehouse
     │
     ├─→ 创建入库单 (InboundOrder)
     │
     └─→ 更新库存可用量

仓库操作员确认出库
     │
     ▼
PUT /api/warehouse/outbound-orders/{id}/confirm
     │
     ├─→ 更新出库单状态
     │
     ├─→ 扣减实际库存
     │
     ├─→ 发送 RocketMQ 消息
     │   Topic: logistics:order:transport
     │   Message: OrderTransportEvent(orderId=xxx, status=40)
     │
     ▼
订单状态 = 已出库(40)
     │
     ▼
[Transport Consumer Group]
     │
     ├─→ 监听 logistics:order:transport
     │
     ├─→ 创建运单 (Waybill)
     │
     └─→ 分配司机和车辆
```

---

## 九、开发调试

### 9.1 查看 Topic 列表

```bash
# 进入 RocketMQ 容器
docker exec -it logistics_rocketmq_broker sh

# 使用 mqadmin 查看 topic
/home/rocketmq/rocketmq-5.3.0/bin/mqadmin topicList -n localhost:9876
```

### 9.2 查看消息积压

```bash
docker exec logistics_rocketmq_broker sh /home/rocketmq/rocketmq-5.3.0/bin/mqadmin consumerProgress -n localhost:9876 -g warehouse-consumer-group
```

### 9.3 消费日志

```bash
docker logs logistics_rocketmq_broker --tail 100 -f | grep "warehouse"
```

### 9.4 重置消费位点（开发用）

```bash
# 重置到最新消息
docker exec logistics_rocketmq_broker sh /home/rocketmq/rocketmq-5.3.0/bin/mqadmin resetOffset -n localhost:9876 \
  -t logistics:order:warehouse -g warehouse-consumer-group -o true
```

---

## 十、注意事项

1. **Broker 内存**：RocketMQ 5.x 默认堆内存按系统内存自动计算（8G系统默认约2G），必须通过 docker `mem_limit` 限制，否则会撑爆虚拟机
2. **端口冲突**：Broker remoting 端口默认 8080，与 Spring Boot 冲突，需通过 `broker.conf` 的 `listenPort` 改为 10811
3. **NameServer 端口**：NameServer 本身端口是 9876，Broker 通过这个端口向 NameServer 注册
4. **Topic 创建**：RocketMQ 支持动态 Topic（首次发送时自动创建），生产环境建议预先创建
5. **消费幂等**：消费者处理消息时要考虑幂等，数据库唯一索引或消息去重表是常用方案
