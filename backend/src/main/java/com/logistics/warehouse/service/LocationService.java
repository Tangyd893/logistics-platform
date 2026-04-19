package com.logistics.warehouse.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.logistics.common.dto.PageDTO;
import com.logistics.common.exception.BusinessException;
import com.logistics.warehouse.domain.dto.LocationCreateRequest;
import com.logistics.warehouse.domain.entity.WhLocation;
import com.logistics.warehouse.repository.WhLocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LocationService {

    @Autowired
    private WhLocationRepository locationRepository;

    public PageDTO<WhLocation> page(Long warehouseId, String keyword, int page, int size) {
        LambdaQueryWrapper<WhLocation> wrapper = new LambdaQueryWrapper<>();
        if (warehouseId != null) wrapper.eq(WhLocation::getWarehouseId, warehouseId);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(WhLocation::getCode, keyword);
        }
        wrapper.orderByAsc(WhLocation::getCode);
        var result = locationRepository.selectPage(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size), wrapper);
        return PageDTO.of(result.getRecords(), result.getTotal(), page, size);
    }

    public List<WhLocation> listByWarehouse(Long warehouseId) {
        return locationRepository.selectList(
            new LambdaQueryWrapper<WhLocation>()
                .eq(warehouseId != null, WhLocation::getWarehouseId, warehouseId)
                .eq(WhLocation::getStatus, 1)
                .orderByAsc(WhLocation::getCode));
    }

    public WhLocation getById(Long id) {
        WhLocation loc = locationRepository.selectById(id);
        if (loc == null) throw new BusinessException("库位不存在");
        return loc;
    }

    @Transactional
    public WhLocation create(LocationCreateRequest req) {
        long count = locationRepository.selectCount(
            new LambdaQueryWrapper<WhLocation>().eq(WhLocation::getCode, req.getCode()));
        if (count > 0) throw new BusinessException("库位编码已存在");
        WhLocation loc = new WhLocation();
        loc.setWarehouseId(req.getWarehouseId());
        loc.setZoneId(req.getZoneId());
        loc.setCode(req.getCode());
        loc.setType(req.getType() != null ? req.getType() : "SHELF");
        loc.setShelfLayer(req.getShelfLayer());
        loc.setCapacity(req.getCapacity());
        loc.setUsedCapacity(java.math.BigDecimal.ZERO);
        loc.setStatus(req.getStatus() != null ? req.getStatus() : 1);
        locationRepository.insert(loc);
        return loc;
    }

    @Transactional
    public WhLocation update(Long id, LocationCreateRequest req) {
        WhLocation loc = locationRepository.selectById(id);
        if (loc == null) throw new BusinessException("库位不存在");
        loc.setCode(req.getCode());
        loc.setZoneId(req.getZoneId());
        if (req.getType() != null) loc.setType(req.getType());
        if (req.getShelfLayer() != null) loc.setShelfLayer(req.getShelfLayer());
        if (req.getCapacity() != null) loc.setCapacity(req.getCapacity());
        if (req.getStatus() != null) loc.setStatus(req.getStatus());
        locationRepository.updateById(loc);
        return loc;
    }

    @Transactional
    public void delete(Long id) {
        locationRepository.deleteById(id);
    }
}
