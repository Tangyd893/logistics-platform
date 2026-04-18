package com.logistics.order.controller;

import com.logistics.common.dto.Result;
import com.logistics.order.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@Tag(name = "统计管理")
@PreAuthorize("hasAnyRole('ADMIN','DISPATCHER','WAREHOUSE_ADMIN')")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @Autowired
    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "仪表盘汇总")
    public Result<Map<String, Object>> getDashboard() {
        return Result.ok(statisticsService.getDashboardSummary());
    }

    @GetMapping("/order")
    @Operation(summary = "订单统计")
    public Result<Map<String, Object>> getOrderStatistics() {
        return Result.ok(statisticsService.getOrderStatistics());
    }

    @GetMapping("/warehouse")
    @Operation(summary = "仓储统计")
    public Result<Map<String, Object>> getWarehouseStatistics() {
        return Result.ok(statisticsService.getWarehouseStatistics());
    }
}
