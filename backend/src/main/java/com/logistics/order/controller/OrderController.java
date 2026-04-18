package com.logistics.order.controller;

import com.logistics.common.dto.PageDTO;
import com.logistics.common.dto.Result;
import com.logistics.order.domain.dto.OrderCreateRequest;
import com.logistics.order.domain.dto.OrderStatusUpdateRequest;
import com.logistics.order.domain.vo.OrderVO;
import com.logistics.order.domain.vo.OrderVO.StatusLogVO;
import com.logistics.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order/orders")
@Tag(name = "订单管理")
@PreAuthorize("hasAnyRole('ADMIN','DISPATCHER','WAREHOUSE_ADMIN','WAREHOUSE_OPERATOR')")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    @Operation(summary = "订单列表（分页）")
    public Result<PageDTO<OrderVO>> page(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String senderName,
            @RequestParam(required = false) String receiverName,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(orderService.page(customerId, keyword, status, senderName, receiverName, page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "订单详情")
    public Result<OrderVO> getById(@PathVariable Long id) {
        return Result.ok(orderService.getById(id));
    }

    @GetMapping("/{orderNo}")
    @Operation(summary = "根据订单号查询")
    public Result<OrderVO> getByOrderNo(@PathVariable String orderNo) {
        return Result.ok(orderService.getByOrderNo(orderNo));
    }

    @PostMapping
    @Operation(summary = "创建订单")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<OrderVO> create(@Valid @RequestBody OrderCreateRequest request) {
        return Result.ok(orderService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改订单")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<OrderVO> update(@PathVariable Long id,
                                  @Valid @RequestBody OrderCreateRequest request) {
        return Result.ok(orderService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除订单")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        orderService.delete(id);
        return Result.ok();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新订单状态")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER','WAREHOUSE_ADMIN','WAREHOUSE_OPERATOR')")
    public Result<OrderVO> updateStatus(@PathVariable Long id,
                                        @Valid @RequestBody OrderStatusUpdateRequest request) {
        return Result.ok(orderService.updateStatus(id, request.getStatus(), request.getRemark()));
    }

    @GetMapping("/{id}/logs")
    @Operation(summary = "订单状态日志")
    public Result<List<StatusLogVO>> getStatusLogs(@PathVariable Long id) {
        return Result.ok(orderService.getStatusLogs(id));
    }
}
