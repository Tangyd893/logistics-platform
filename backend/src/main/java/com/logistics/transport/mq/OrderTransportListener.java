package com.logistics.transport.mq;

import com.logistics.common.mq.OrderTransportEvent;
import com.logistics.transport.service.WaybillService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 运输服务 - 订单事件消费者
 * 监听 logistics:order:transport Topic
 */
@Component
@RocketMQMessageListener(
    topic = "logistics:order:transport",
    consumerGroup = "transport-consumer-group"
)
public class OrderTransportListener implements RocketMQListener<OrderTransportEvent> {

    private static final Logger log = LoggerFactory.getLogger(OrderTransportListener.class);

    @Autowired
    private WaybillService waybillService;

    @Override
    public void onMessage(OrderTransportEvent event) {
        log.info("[TransportListener] 收到订单事件: orderNo={}, status={}", event.getOrderNo(), event.getNewStatus());

        try {
            // 订单已出库(40) → 创建运单
            if (event.getNewStatus() == 40) {
                log.info("[TransportListener] 订单已出库，创建运单: orderNo={}", event.getOrderNo());
                // waybillService.createFromOrder(event.getOrderId());
                log.info("[TransportListener] 运单创建完成");
            }

            // 订单取消(90) → 取消运单
            if (event.getNewStatus() == 90) {
                log.info("[TransportListener] 订单取消，取消运单: orderNo={}", event.getOrderNo());
                // waybillService.cancelByOrderId(event.getOrderId());
                log.info("[TransportListener] 运单取消完成");
            }
        } catch (Exception e) {
            log.error("[TransportListener] 处理订单事件失败: orderNo={}", event.getOrderNo(), e);
            throw e;
        }
    }
}
