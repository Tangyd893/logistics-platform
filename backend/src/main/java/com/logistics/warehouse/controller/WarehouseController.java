package com.logistics.warehouse.controller;

import com.logistics.common.dto.PageDTO;
import com.logistics.common.dto.Result;
import com.logistics.warehouse.domain.dto.WarehouseCreateRequest;
import com.logistics.warehouse.domain.vo.WarehouseVO;
import com.logistics.warehouse.service.WarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warehouse/warehouses")
@Tag(name = "仓库管理")
@PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_ADMIN','WAREHOUSE_OPERATOR')")
public class WarehouseController {

    private final WarehouseService warehouseService;

    @Autowired
    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @GetMapping
    @Operation(summary = "仓库列表（分页）")
    public Result<PageDTO<WarehouseVO>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(warehouseService.page(keyword, status, page, size));
    }

    @GetMapping("/all")
    @Operation(summary = "所有启用的仓库（下拉框用）")
    public Result<List<WarehouseVO>> listAll() {
        return Result.ok(warehouseService.listAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "仓库详情")
    public Result<WarehouseVO> getById(@PathVariable Long id) {
        return Result.ok(warehouseService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建仓库")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_ADMIN')")
    public Result<WarehouseVO> create(@Valid @RequestBody WarehouseCreateRequest request) {
        return Result.ok(warehouseService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改仓库")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_ADMIN')")
    public Result<WarehouseVO> update(@PathVariable Long id,
                                       @Valid @RequestBody WarehouseCreateRequest request) {
        return Result.ok(warehouseService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除仓库")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        warehouseService.delete(id);
        return Result.ok();
    }
}
