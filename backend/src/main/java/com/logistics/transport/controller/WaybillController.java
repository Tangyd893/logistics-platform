package com.logistics.transport.controller;

import com.logistics.common.dto.PageDTO;
import com.logistics.common.dto.Result;
import com.logistics.transport.domain.dto.WaybillCreateRequest;
import com.logistics.transport.domain.vo.WaybillVO;
import com.logistics.transport.domain.vo.TrackingVO;
import com.logistics.transport.service.WaybillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transport/waybills")
@Tag(name = "运单管理")
@PreAuthorize("hasAnyRole('ADMIN','DISPATCHER','DRIVER')")
public class WaybillController {

    private final WaybillService waybillService;

    @Autowired
    public WaybillController(WaybillService waybillService) {
        this.waybillService = waybillService;
    }

    @GetMapping
    @Operation(summary = "运单列表（分页）")
    public Result<PageDTO<WaybillVO>> page(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long driverId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String waybillNo,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(waybillService.page(warehouseId, driverId, status, waybillNo, page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "运单详情")
    public Result<WaybillVO> getById(@PathVariable Long id) {
        return Result.ok(waybillService.getById(id));
    }

    @GetMapping("/no/{waybillNo}")
    @Operation(summary = "根据运单号查询")
    public Result<WaybillVO> getByWaybillNo(@PathVariable String waybillNo) {
        return Result.ok(waybillService.getByWaybillNo(waybillNo));
    }

    @PostMapping
    @Operation(summary = "创建运单")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<WaybillVO> create(@Valid @RequestBody WaybillCreateRequest request) {
        return Result.ok(waybillService.create(request));
    }

    @PostMapping("/{id}/confirm-pickup")
    @Operation(summary = "确认提货")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER','DRIVER')")
    public Result<WaybillVO> confirmPickup(@PathVariable Long id) {
        return Result.ok(waybillService.confirmPickup(id));
    }

    @PostMapping("/{id}/confirm-delivery")
    @Operation(summary = "确认送达")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER','DRIVER')")
    public Result<WaybillVO> confirmDelivery(@PathVariable Long id) {
        return Result.ok(waybillService.confirmDelivery(id));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "拒收")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER','DRIVER')")
    public Result<WaybillVO> reject(@PathVariable Long id,
                                    @RequestParam(required = false) String reason) {
        return Result.ok(waybillService.reject(id, reason));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "取消运单")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Void> cancel(@PathVariable Long id) {
        waybillService.cancel(id);
        return Result.ok();
    }

    @GetMapping("/{id}/trackings")
    @Operation(summary = "运单轨迹")
    public Result<List<TrackingVO>> getTrackings(@PathVariable Long id) {
        return Result.ok(waybillService.getTrackings(id));
    }
}
