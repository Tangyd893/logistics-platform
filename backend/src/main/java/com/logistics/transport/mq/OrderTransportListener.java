package com.logistics.transport.mq;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.logistics.common.mq.OrderTransportEvent;
import com.logistics.transport.domain.dto.WaybillCreateRequest;
import com.logistics.transport.domain.entity.TDriver;
import com.logistics.transport.domain.entity.TVehicle;
import com.logistics.transport.repository.TDriverRepository;
import com.logistics.transport.repository.TVehicleRepository;
import com.logistics.transport.service.WaybillService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 运输服务 - 订单事件消费者
 * 监听 logistics:order:transport Topic
 *
 * 事件处理策略：
 * - status=40（已出库）→ 自动创建运单（自动分配空闲司机+车辆）
 * - status=90（已取消）→ 取消对应运单（待扩展）
 */
@Component
@RocketMQMessageListener(
    topic = "logistics_order_transport",
    consumerGroup = "transport-consumer-group"
)
public class OrderTransportListener implements RocketMQListener<OrderTransportEvent> {

    private static final Logger log = LoggerFactory.getLogger(OrderTransportListener.class);

    @Autowired
    private WaybillService waybillService;

    @Autowired
    private TDriverRepository driverRepository;

    @Autowired
    private TVehicleRepository vehicleRepository;

    @Override
    public void onMessage(OrderTransportEvent event) {
        log.info("[TransportListener] 收到订单事件: orderNo={}, status={}", event.getOrderNo(), event.getNewStatus());

        try {
            switch (event.getNewStatus()) {
                case 40 -> handleOrderShipped(event);
                case 90 -> handleOrderCancelled(event);
                default -> log.debug("[TransportListener] 订单状态 {} 无需处理", event.getNewStatus());
            }
        } catch (Exception e) {
            log.error("[TransportListener] 处理订单事件失败: orderNo={}", event.getOrderNo(), e);
            throw e;
        }
    }

    private void handleOrderShipped(OrderTransportEvent event) {
        log.info("[TransportListener] 订单已出库，创建运单: orderNo={}", event.getOrderNo());

        // 自动分配空闲司机
        TDriver driver = findAvailableDriver();
        if (driver == null) {
            log.warn("[TransportListener] 无可用司机，无法自动创建运单: orderNo={}", event.getOrderNo());
            return;
        }

        // 自动分配空闲车辆
        TVehicle vehicle = findAvailableVehicle();
        if (vehicle == null) {
            log.warn("[TransportListener] 无可用车辆，无法自动创建运单: orderNo={}", event.getOrderNo());
            return;
        }

        // 构建运单
        WaybillCreateRequest request = new WaybillCreateRequest();
        request.setOrderId(event.getOrderId());
        request.setWarehouseId(1L); // TODO: 根据订单地址 routing
        request.setDriverId(driver.getId());
        request.setVehicleId(vehicle.getId());
        request.setPlanPickupTime(java.time.LocalDateTime.now());
        request.setPlanDeliveryTime(java.time.LocalDateTime.now().plusDays(2));

        var result = waybillService.create(request);
        log.info("[TransportListener] 运单创建成功: waybillNo={}, orderNo={}, driver={}, vehicle={}",
                result.getWaybillNo(), event.getOrderNo(), driver.getName(), vehicle.getPlateNo());
    }

    private void handleOrderCancelled(OrderTransportEvent event) {
        log.info("[TransportListener] 订单已取消，取消运单: orderNo={}", event.getOrderNo());
        // TODO: 根据 orderId 查找运单并取消
        // waybillService.cancelByOrderId(event.getOrderId());
        log.info("[TransportListener] 订单 {} 无需取消运单（暂无关联）", event.getOrderNo());
    }

    /**
     * 查找第一个空闲司机
     */
    private TDriver findAvailableDriver() {
        List<TDriver> drivers = driverRepository.selectList(
            new LambdaQueryWrapper<TDriver>()
                .eq(TDriver::getStatus, 1) // 1=空闲
                .last("LIMIT 1")
        );
        return drivers.isEmpty() ? null : drivers.get(0);
    }

    /**
     * 查找第一辆空闲车辆
     */
    private TVehicle findAvailableVehicle() {
        List<TVehicle> vehicles = vehicleRepository.selectList(
            new LambdaQueryWrapper<TVehicle>()
                .eq(TVehicle::getStatus, 1) // 1=空闲
                .last("LIMIT 1")
        );
        return vehicles.isEmpty() ? null : vehicles.get(0);
    }
}
