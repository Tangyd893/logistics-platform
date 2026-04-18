package com.logistics.warehouse.controller;

import com.logistics.common.dto.PageDTO;
import com.logistics.common.dto.Result;
import com.logistics.warehouse.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/warehouse/inventory")
@Tag(name = "库存管理")
@PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_ADMIN','WAREHOUSE_OPERATOR')")
public class InventoryController {

    private final InventoryService inventoryService;

    @Autowired
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    @Operation(summary = "库存列表（分页）")
    public Result<PageDTO<Map<String, Object>>> page(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) String sku,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(inventoryService.page(warehouseId, locationId, sku, keyword, status, page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "库存详情")
    public Result<Map<String, Object>> getById(@PathVariable Long id) {
        return Result.ok(inventoryService.getById(id));
    }

    @GetMapping("/low-stock-alerts")
    @Operation(summary = "低库存预警")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_ADMIN')")
    public Result<List<Map<String, Object>>> getLowStockAlerts(
            @RequestParam(required = false) Long warehouseId) {
        return Result.ok(inventoryService.getLowStockAlerts(warehouseId));
    }

    @PostMapping("/{id}/freeze")
    @Operation(summary = "冻结库存")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_ADMIN')")
    public Result<Void> freeze(@PathVariable Long id,
                               @RequestParam BigDecimal freezeQty) {
        inventoryService.freeze(id, freezeQty);
        return Result.ok();
    }

    @PostMapping("/{id}/unfreeze")
    @Operation(summary = "解除冻结")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_ADMIN')")
    public Result<Void> unfreeze(@PathVariable Long id) {
        inventoryService.unfreeze(id);
        return Result.ok();
    }

    @PostMapping("/{id}/report-damage")
    @Operation(summary = "报损")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_ADMIN')")
    public Result<Void> reportDamage(@PathVariable Long id,
                                     @RequestParam BigDecimal damageQty) {
        inventoryService.reportDamage(id, damageQty);
        return Result.ok();
    }
}
