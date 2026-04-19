package com.logistics.warehouse.mq;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.logistics.common.mq.OrderWarehouseEvent;
import com.logistics.warehouse.domain.dto.InboundOrderCreateRequest;
import com.logistics.warehouse.domain.entity.WhWarehouse;
import com.logistics.warehouse.repository.WhWarehouseRepository;
import com.logistics.warehouse.service.InboundOrderService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 仓库服务 - 订单事件消费者
 * 监听 logistics:order:warehouse Topic
 *
 * 事件处理策略：
 * - status=20（已确认）→ 创建入库单（自动选择第一个仓库）
 * - status=90（已取消）→ 作废对应入库单（待扩展）
 */
@Component
@RocketMQMessageListener(
    topic = "logistics_order_warehouse",
    consumerGroup = "warehouse-consumer-group"
)
public class OrderWarehouseListener implements RocketMQListener<OrderWarehouseEvent> {

    private static final Logger log = LoggerFactory.getLogger(OrderWarehouseListener.class);

    @Autowired
    private InboundOrderService inboundOrderService;

    @Autowired
    private WhWarehouseRepository warehouseRepository;

    @Override
    public void onMessage(OrderWarehouseEvent event) {
        log.info("[WarehouseListener] 收到订单事件: orderNo={}, status={}", event.getOrderNo(), event.getNewStatus());

        try {
            switch (event.getNewStatus()) {
                case 20 -> handleOrderConfirmed(event);
                case 90 -> handleOrderCancelled(event);
                default -> log.debug("[WarehouseListener] 订单状态 {} 无需处理", event.getNewStatus());
            }
        } catch (Exception e) {
            log.error("[WarehouseListener] 处理订单事件失败: orderNo={}", event.getOrderNo(), e);
            throw e; // 消费失败时 RocketMQ 自动重试
        }
    }

    private void handleOrderConfirmed(OrderWarehouseEvent event) {
        log.info("[WarehouseListener] 订单已确认，创建入库单: orderNo={}", event.getOrderNo());

        // 自动选择第一个仓库（生产环境应按地域/品类路由）
        Long warehouseId = findDefaultWarehouseId();
        if (warehouseId == null) {
            log.warn("[WarehouseListener] 未找到可用仓库，无法创建入库单: orderNo={}", event.getOrderNo());
            return;
        }

        // 构建入库单
        InboundOrderCreateRequest request = new InboundOrderCreateRequest();
        request.setWarehouseId(warehouseId);
        request.setSupplierName(event.getSenderName() != null ? event.getSenderName() : "供应商");
        request.setInboundType("采购入库");
        request.setRemark("来源订单: " + event.getOrderNo());
        request.setExpectedArrivalTime(java.time.LocalDateTime.now().plusDays(1));

        // 创建入库单
        var result = inboundOrderService.create(request);
        log.info("[WarehouseListener] 入库单创建成功: inboundOrderNo={}, orderNo={}", result.getOrderNo(), event.getOrderNo());
    }

    private void handleOrderCancelled(OrderWarehouseEvent event) {
        log.info("[WarehouseListener] 订单已取消，作废入库单: orderNo={}", event.getOrderNo());
        // TODO: 根据 orderNo 查找入库单并作废
        // inboundOrderService.cancelByOrderNo(event.getOrderNo());
        log.info("[WarehouseListener] 订单 {} 无需作废入库单（暂无关联）", event.getOrderNo());
    }

    private Long findDefaultWarehouseId() {
        // 查找第一个状态正常的仓库
        List<WhWarehouse> warehouses = warehouseRepository.selectList(
            new LambdaQueryWrapper<WhWarehouse>()
                .eq(WhWarehouse::getStatus, 1)
                .last("LIMIT 1")
        );
        return warehouses.isEmpty() ? null : warehouses.get(0).getId();
    }
}
