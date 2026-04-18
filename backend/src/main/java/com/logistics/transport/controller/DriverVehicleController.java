package com.logistics.transport.controller;

import com.logistics.common.dto.PageDTO;
import com.logistics.common.dto.Result;
import com.logistics.transport.domain.entity.TDriver;
import com.logistics.transport.domain.entity.TVehicle;
import com.logistics.transport.repository.TDriverRepository;
import com.logistics.transport.repository.TVehicleRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transport")
@Tag(name = "司机车辆管理")
@PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
public class DriverVehicleController {

    private final TDriverRepository driverRepository;
    private final TVehicleRepository vehicleRepository;

    @Autowired
    public DriverVehicleController(TDriverRepository driverRepository,
                                   TVehicleRepository vehicleRepository) {
        this.driverRepository = driverRepository;
        this.vehicleRepository = vehicleRepository;
    }

    // ==================== 司机管理 ====================

    @GetMapping("/drivers")
    @Operation(summary = "司机列表")
    public Result<List<TDriver>> listDrivers(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Integer status) {
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TDriver>();
        if (warehouseId != null) {
            wrapper.eq(TDriver::getWarehouseId, warehouseId);
        }
        if (status != null) {
            wrapper.eq(TDriver::getStatus, status);
        }
        wrapper.orderByAsc(TDriver::getName);
        return Result.ok(driverRepository.selectList(wrapper));
    }

    @GetMapping("/drivers/{id}")
    @Operation(summary = "司机详情")
    public Result<TDriver> getDriver(@PathVariable Long id) {
        return Result.ok(driverRepository.selectById(id));
    }

    @PostMapping("/drivers")
    @Operation(summary = "创建司机")
    public Result<TDriver> createDriver(@RequestBody TDriver driver) {
        driver.setStatus(1);
        driverRepository.insert(driver);
        return Result.ok(driver);
    }

    @PutMapping("/drivers/{id}")
    @Operation(summary = "更新司机")
    public Result<TDriver> updateDriver(@PathVariable Long id, @RequestBody TDriver driver) {
        driver.setId(id);
        driverRepository.updateById(driver);
        return Result.ok(driver);
    }

    // ==================== 车辆管理 ====================

    @GetMapping("/vehicles")
    @Operation(summary = "车辆列表")
    public Result<List<TVehicle>> listVehicles(@RequestParam(required = false) Integer status) {
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TVehicle>();
        if (status != null) {
            wrapper.eq(TVehicle::getStatus, status);
        }
        wrapper.orderByAsc(TVehicle::getPlateNo);
        return Result.ok(vehicleRepository.selectList(wrapper));
    }

    @GetMapping("/vehicles/{id}")
    @Operation(summary = "车辆详情")
    public Result<TVehicle> getVehicle(@PathVariable Long id) {
        return Result.ok(vehicleRepository.selectById(id));
    }

    @PostMapping("/vehicles")
    @Operation(summary = "创建车辆")
    public Result<TVehicle> createVehicle(@RequestBody TVehicle vehicle) {
        vehicle.setStatus(1);
        vehicleRepository.insert(vehicle);
        return Result.ok(vehicle);
    }

    @PutMapping("/vehicles/{id}")
    @Operation(summary = "更新车辆")
    public Result<TVehicle> updateVehicle(@PathVariable Long id, @RequestBody TVehicle vehicle) {
        vehicle.setId(id);
        vehicleRepository.updateById(vehicle);
        return Result.ok(vehicle);
    }
}
