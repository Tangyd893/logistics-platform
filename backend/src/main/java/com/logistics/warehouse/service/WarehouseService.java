package com.logistics.warehouse.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.logistics.common.dto.PageDTO;
import com.logistics.common.exception.BusinessException;
import com.logistics.warehouse.domain.dto.WarehouseCreateRequest;
import com.logistics.warehouse.domain.entity.WhWarehouse;
import com.logistics.warehouse.domain.vo.WarehouseVO;
import com.logistics.warehouse.repository.WhWarehouseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class WarehouseService {

    private static final Logger log = LoggerFactory.getLogger(WarehouseService.class);

    private final WhWarehouseRepository warehouseRepository;

    @Autowired
    public WarehouseService(WhWarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    /**
     * 分页查询仓库
     */
    public PageDTO<WarehouseVO> page(String keyword, Integer status, int page, int size) {
        LambdaQueryWrapper<WhWarehouse> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(WhWarehouse::getCode, keyword)
                    .or().like(WhWarehouse::getName, keyword));
        }
        if (status != null) {
            wrapper.eq(WhWarehouse::getStatus, status);
        }
        wrapper.orderByDesc(WhWarehouse::getCreatedAt);

        Page<WhWarehouse> result = warehouseRepository.selectPage(new Page<>(page, size), wrapper);
        List<WarehouseVO> voList = result.getRecords().stream().map(this::toVO).toList();
        return PageDTO.of(voList, result.getTotal(), page, size);
    }

    /**
     * 根据ID查询仓库
     */
    public WarehouseVO getById(Long id) {
        WhWarehouse warehouse = warehouseRepository.selectById(id);
        if (warehouse == null) {
            throw new BusinessException("仓库不存在");
        }
        return toVO(warehouse);
    }

    /**
     * 查询所有启用的仓库
     */
    public List<WarehouseVO> listAll() {
        LambdaQueryWrapper<WhWarehouse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WhWarehouse::getStatus, 1).orderByAsc(WhWarehouse::getCode);
        return warehouseRepository.selectList(wrapper).stream().map(this::toVO).toList();
    }

    /**
     * 创建仓库
     */
    @Transactional
    public WarehouseVO create(WarehouseCreateRequest request) {
        // 检查编码唯一性
        LambdaQueryWrapper<WhWarehouse> codeCheck = new LambdaQueryWrapper<>();
        codeCheck.eq(WhWarehouse::getCode, request.getCode());
        if (warehouseRepository.selectCount(codeCheck) > 0) {
            throw new BusinessException("仓库编码已存在");
        }

        WhWarehouse warehouse = new WhWarehouse();
        warehouse.setCode(request.getCode());
        warehouse.setName(request.getName());
        warehouse.setAddress(request.getAddress());
        warehouse.setManager(request.getManager());
        warehouse.setPhone(request.getPhone());
        warehouse.setTotalCapacity(request.getTotalCapacity());
        warehouse.setUsedCapacity(BigDecimal.ZERO);
        warehouse.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        warehouse.setRemark(request.getRemark());

        warehouseRepository.insert(warehouse);
        log.info("创建仓库: {}", request.getCode());
        return toVO(warehouse);
    }

    /**
     * 更新仓库
     */
    @Transactional
    public WarehouseVO update(Long id, WarehouseCreateRequest request) {
        WhWarehouse warehouse = warehouseRepository.selectById(id);
        if (warehouse == null) {
            throw new BusinessException("仓库不存在");
        }

        // 检查编码唯一性（排除自己）
        LambdaQueryWrapper<WhWarehouse> codeCheck = new LambdaQueryWrapper<>();
        codeCheck.eq(WhWarehouse::getCode, request.getCode()).ne(WhWarehouse::getId, id);
        if (warehouseRepository.selectCount(codeCheck) > 0) {
            throw new BusinessException("仓库编码已存在");
        }

        warehouse.setName(request.getName());
        warehouse.setAddress(request.getAddress());
        warehouse.setManager(request.getManager());
        warehouse.setPhone(request.getPhone());
        warehouse.setTotalCapacity(request.getTotalCapacity());
        if (request.getStatus() != null) {
            warehouse.setStatus(request.getStatus());
        }
        warehouse.setRemark(request.getRemark());

        warehouseRepository.updateById(warehouse);
        log.info("更新仓库: {}", warehouse.getCode());
        return toVO(warehouse);
    }

    /**
     * 删除仓库
     */
    @Transactional
    public void delete(Long id) {
        WhWarehouse warehouse = warehouseRepository.selectById(id);
        if (warehouse == null) {
            throw new BusinessException("仓库不存在");
        }
        warehouseRepository.deleteById(id);
        log.info("删除仓库: {}", warehouse.getCode());
    }

    private WarehouseVO toVO(WhWarehouse w) {
        WarehouseVO vo = new WarehouseVO();
        vo.setId(w.getId());
        vo.setCode(w.getCode());
        vo.setName(w.getName());
        vo.setAddress(w.getAddress());
        vo.setManager(w.getManager());
        vo.setPhone(w.getPhone());
        vo.setTotalCapacity(w.getTotalCapacity());
        vo.setUsedCapacity(w.getUsedCapacity());
        if (w.getTotalCapacity() != null && w.getUsedCapacity() != null) {
            vo.setAvailableCapacity(w.getTotalCapacity().subtract(w.getUsedCapacity()));
        }
        vo.setStatus(w.getStatus());
        vo.setStatusName(w.getStatus() == 1 ? "启用" : "停用");
        vo.setRemark(w.getRemark());
        return vo;
    }
}
