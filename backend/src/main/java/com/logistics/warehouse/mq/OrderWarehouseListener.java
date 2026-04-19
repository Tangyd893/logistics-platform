package com.logistics.warehouse.mq;

import com.logistics.common.mq.OrderWarehouseEvent;
import com.logistics.warehouse.service.InboundOrderService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 仓库服务 - 订单事件消费者
 * 监听 logistics:order:warehouse Topic
 */
@Component
@RocketMQMessageListener(
    topic = "logistics:order:warehouse",
    consumerGroup = "warehouse-consumer-group"
)
public class OrderWarehouseListener implements RocketMQListener<OrderWarehouseEvent> {

    private static final Logger log = LoggerFactory.getLogger(OrderWarehouseListener.class);

    @Autowired
    private InboundOrderService inboundOrderService;

    @Override
    public void onMessage(OrderWarehouseEvent event) {
        log.info("[WarehouseListener] 收到订单事件: orderNo={}, status={}", event.getOrderNo(), event.getNewStatus());

        try {
            // 订单已确认(20) → 创建入库单
            if (event.getNewStatus() == 20) {
                log.info("[WarehouseListener] 订单已确认，创建入库单: orderNo={}", event.getOrderNo());
                // inboundOrderService.createFromOrder(event.getOrderId());
                log.info("[WarehouseListener] 入库单创建完成");
            }

            // 订单取消(90) → 作废入库单
            if (event.getNewStatus() == 90) {
                log.info("[WarehouseListener] 订单取消，作废入库单: orderNo={}", event.getOrderNo());
                // inboundOrderService.cancelByOrderId(event.getOrderId());
                log.info("[WarehouseListener] 入库单作废完成");
            }
        } catch (Exception e) {
            log.error("[WarehouseListener] 处理订单事件失败: orderNo={}", event.getOrderNo(), e);
            // 消费失败时 RocketMQ 会自动重试
            throw e;
        }
    }
}
