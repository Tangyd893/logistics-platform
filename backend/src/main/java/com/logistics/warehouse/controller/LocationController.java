package com.logistics.warehouse.controller;

import com.logistics.common.dto.Result;
import com.logistics.warehouse.domain.dto.LocationCreateRequest;
import com.logistics.warehouse.domain.entity.WhLocation;
import com.logistics.warehouse.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warehouse/locations")
@Tag(name = "库位管理")
@PreAuthorize("hasRole('ADMIN')")
public class LocationController {

    @Autowired
    private LocationService locationService;

    @GetMapping
    @Operation(summary = "库位列表（分页）")
    public Result<?> page(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        return Result.ok(locationService.page(warehouseId, keyword, page, size));
    }

    @GetMapping("/warehouse/{warehouseId}")
    @Operation(summary = "按仓库查库位（下拉框用）")
    public Result<List<WhLocation>> listByWarehouse(@PathVariable Long warehouseId) {
        return Result.ok(locationService.listByWarehouse(warehouseId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "库位详情")
    public Result<WhLocation> getById(@PathVariable Long id) {
        return Result.ok(locationService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建库位")
    public Result<WhLocation> create(@Valid @RequestBody LocationCreateRequest request) {
        return Result.ok(locationService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新库位")
    public Result<WhLocation> update(@PathVariable Long id, @Valid @RequestBody LocationCreateRequest request) {
        return Result.ok(locationService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除库位")
    public Result<?> delete(@PathVariable Long id) {
        locationService.delete(id);
        return Result.ok(null);
    }
}
