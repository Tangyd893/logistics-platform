package com.logistics.warehouse.controller;

import com.logistics.common.dto.PageDTO;
import com.logistics.common.dto.Result;
import com.logistics.warehouse.domain.dto.OutboundOrderCreateRequest;
import com.logistics.warehouse.domain.vo.OutboundOrderVO;
import com.logistics.warehouse.service.OutboundOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/warehouse/outbound-orders")
@Tag(name = "出库管理")
@PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_ADMIN','WAREHOUSE_OPERATOR')")
public class OutboundOrderController {

    private final OutboundOrderService outboundOrderService;

    @Autowired
    public OutboundOrderController(OutboundOrderService outboundOrderService) {
        this.outboundOrderService = outboundOrderService;
    }

    @GetMapping
    @Operation(summary = "出库单列表（分页）")
    public Result<PageDTO<OutboundOrderVO>> page(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(outboundOrderService.page(warehouseId, keyword, status, page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "出库单详情")
    public Result<OutboundOrderVO> getById(@PathVariable Long id) {
        return Result.ok(outboundOrderService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建出库单")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_ADMIN')")
    public Result<OutboundOrderVO> create(@Valid @RequestBody OutboundOrderCreateRequest request) {
        return Result.ok(outboundOrderService.create(request));
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "确认出库单")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_ADMIN')")
    public Result<OutboundOrderVO> confirm(@PathVariable Long id) {
        return Result.ok(outboundOrderService.confirm(id));
    }

    @PostMapping("/{id}/start-picking")
    @Operation(summary = "开始拣货")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_ADMIN','WAREHOUSE_OPERATOR')")
    public Result<OutboundOrderVO> startPicking(@PathVariable Long id) {
        return Result.ok(outboundOrderService.startPicking(id));
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "完成出库")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_ADMIN','WAREHOUSE_OPERATOR')")
    public Result<OutboundOrderVO> completeOutbound(@PathVariable Long id) {
        return Result.ok(outboundOrderService.completeOutbound(id));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "取消出库单")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_ADMIN')")
    public Result<Void> cancel(@PathVariable Long id) {
        outboundOrderService.cancel(id);
        return Result.ok();
    }
}
