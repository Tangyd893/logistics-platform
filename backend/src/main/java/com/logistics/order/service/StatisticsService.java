package com.logistics.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.logistics.order.repository.OOrderRepository;
import com.logistics.warehouse.repository.WhInboundOrderRepository;
import com.logistics.warehouse.repository.WhOutboundOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class StatisticsService {

    private final OOrderRepository orderRepository;
    private final WhInboundOrderRepository inboundOrderRepository;
    private final WhOutboundOrderRepository outboundOrderRepository;

    @Autowired
    public StatisticsService(OOrderRepository orderRepository,
                            WhInboundOrderRepository inboundOrderRepository,
                            WhOutboundOrderRepository outboundOrderRepository) {
        this.orderRepository = orderRepository;
        this.inboundOrderRepository = inboundOrderRepository;
        this.outboundOrderRepository = outboundOrderRepository;
    }

    /**
     * 订单统计
     */
    public Map<String, Object> getOrderStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 订单总数
        long totalOrders = orderRepository.selectCount(null);

        // 各状态订单数
        Map<Integer, Long> statusCounts = new HashMap<>();
        for (int status = 10; status <= 80; status += 10) {
            LambdaQueryWrapper<com.logistics.order.domain.entity.OOrder> wrapper =
                    new LambdaQueryWrapper<>();
            wrapper.eq(com.logistics.order.domain.entity.OOrder::getStatus, status);
            statusCounts.put(status, orderRepository.selectCount(wrapper));
        }

        stats.put("totalOrders", totalOrders);
        stats.put("statusCounts", statusCounts);

        return stats;
    }

    /**
     * 仓储统计
     */
    public Map<String, Object> getWarehouseStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 入库单统计
        long totalInbound = inboundOrderRepository.selectCount(null);
        LambdaQueryWrapper<com.logistics.warehouse.domain.entity.WhInboundOrder> inboundWrapper =
                new LambdaQueryWrapper<>();
        inboundWrapper.eq(com.logistics.warehouse.domain.entity.WhInboundOrder::getStatus, 40);
        long completedInbound = inboundOrderRepository.selectCount(inboundWrapper);

        // 出库单统计
        long totalOutbound = outboundOrderRepository.selectCount(null);
        LambdaQueryWrapper<com.logistics.warehouse.domain.entity.WhOutboundOrder> outboundWrapper =
                new LambdaQueryWrapper<>();
        outboundWrapper.eq(com.logistics.warehouse.domain.entity.WhOutboundOrder::getStatus, 40);
        long completedOutbound = outboundOrderRepository.selectCount(outboundWrapper);

        stats.put("totalInbound", totalInbound);
        stats.put("completedInbound", completedInbound);
        stats.put("totalOutbound", totalOutbound);
        stats.put("completedOutbound", completedOutbound);

        return stats;
    }

    /**
     * 仪表盘汇总
     */
    public Map<String, Object> getDashboardSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("orders", getOrderStatistics());
        summary.put("warehouse", getWarehouseStatistics());
        return summary;
    }
}
