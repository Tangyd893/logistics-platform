package com.logistics.warehouse.controller;

import com.logistics.common.dto.PageDTO;
import com.logistics.common.dto.Result;
import com.logistics.warehouse.domain.dto.InboundOrderCreateRequest;
import com.logistics.warehouse.domain.vo.InboundOrderVO;
import com.logistics.warehouse.service.InboundOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/warehouse/inbound-orders")
@Tag(name = "入库管理")
@PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_ADMIN','WAREHOUSE_OPERATOR')")
public class InboundOrderController {

    private final InboundOrderService inboundOrderService;

    @Autowired
    public InboundOrderController(InboundOrderService inboundOrderService) {
        this.inboundOrderService = inboundOrderService;
    }

    @GetMapping
    @Operation(summary = "入库单列表（分页）")
    public Result<PageDTO<InboundOrderVO>> page(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(inboundOrderService.page(warehouseId, keyword, status, page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "入库单详情")
    public Result<InboundOrderVO> getById(@PathVariable Long id) {
        return Result.ok(inboundOrderService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建入库单")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_ADMIN')")
    public Result<InboundOrderVO> create(@Valid @RequestBody InboundOrderCreateRequest request) {
        return Result.ok(inboundOrderService.create(request));
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "确认入库单")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_ADMIN')")
    public Result<InboundOrderVO> confirm(@PathVariable Long id) {
        return Result.ok(inboundOrderService.confirm(id));
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "开始入库")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_ADMIN','WAREHOUSE_OPERATOR')")
    public Result<InboundOrderVO> startInbound(@PathVariable Long id) {
        return Result.ok(inboundOrderService.startInbound(id, null));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "取消入库单")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_ADMIN')")
    public Result<Void> cancel(@PathVariable Long id) {
        inboundOrderService.cancel(id);
        return Result.ok();
    }
}
