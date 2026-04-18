package com.logistics.warehouse.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.logistics.common.dto.PageDTO;
import com.logistics.common.exception.BusinessException;
import com.logistics.warehouse.domain.entity.WhInventory;
import com.logistics.warehouse.domain.entity.WhWarehouse;
import com.logistics.warehouse.repository.WhInventoryRepository;
import com.logistics.warehouse.repository.WhWarehouseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    private final WhInventoryRepository inventoryRepository;
    private final WhWarehouseRepository warehouseRepository;

    @Autowired
    public InventoryService(WhInventoryRepository inventoryRepository,
                            WhWarehouseRepository warehouseRepository) {
        this.inventoryRepository = inventoryRepository;
        this.warehouseRepository = warehouseRepository;
    }

    /**
     * 分页查询库存
     */
    public PageDTO<Map<String, Object>> page(Long warehouseId, Long locationId,
                                              String sku, String keyword,
                                              Integer status, int page, int size) {
        LambdaQueryWrapper<WhInventory> wrapper = new LambdaQueryWrapper<>();
        if (warehouseId != null) {
            wrapper.eq(WhInventory::getWarehouseId, warehouseId);
        }
        if (locationId != null) {
            wrapper.eq(WhInventory::getLocationId, locationId);
        }
        if (sku != null && !sku.isBlank()) {
            wrapper.eq(WhInventory::getSku, sku);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(WhInventory::getSku, keyword)
                    .or().like(WhInventory::getGoodsName, keyword));
        }
        if (status != null) {
            wrapper.eq(WhInventory::getStatus, status);
        }
        wrapper.orderByDesc(WhInventory::getCreatedAt);

        Page<WhInventory> result = inventoryRepository.selectPage(new Page<>(page, size), wrapper);

        // 转换为 Map 列表（含仓库名）
        List<Map<String, Object>> records = result.getRecords().stream().map(inv -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", inv.getId());
            m.put("warehouseId", inv.getWarehouseId());
            m.put("locationId", inv.getLocationId());
            m.put("sku", inv.getSku());
            m.put("goodsName", inv.getGoodsName());
            m.put("quantity", inv.getQuantity());
            m.put("unit", inv.getUnit());
            m.put("unitPrice", inv.getUnitPrice());
            m.put("totalValue", inv.getTotalValue());
            m.put("batchNo", inv.getBatchNo());
            m.put("productionDate", inv.getProductionDate());
            m.put("expiryDate", inv.getExpiryDate());
            m.put("status", inv.getStatus());
            m.put("statusName", getStatusName(inv.getStatus()));

            WhWarehouse wh = warehouseRepository.selectById(inv.getWarehouseId());
            if (wh != null) {
                m.put("warehouseName", wh.getName());
            }
            return m;
        }).toList();

        return PageDTO.of(records, result.getTotal(), page, size);
    }

    /**
     * 根据ID查询库存详情
     */
    public Map<String, Object> getById(Long id) {
        WhInventory inv = inventoryRepository.selectById(id);
        if (inv == null) {
            throw new BusinessException("库存记录不存在");
        }
        return toMap(inv);
    }

    /**
     * 冻结库存
     */
    @Transactional
    public void freeze(Long id, BigDecimal freezeQty) {
        WhInventory inv = inventoryRepository.selectById(id);
        if (inv == null) {
            throw new BusinessException("库存记录不存在");
        }
        if (inv.getQuantity().compareTo(freezeQty) < 0) {
            throw new BusinessException("冻结数量不能大于可用数量");
        }
        inv.setStatus(2); // 冻结
        inventoryRepository.updateById(inv);
        log.info("冻结库存: id={}, qty={}", id, freezeQty);
    }

    /**
     * 解除冻结
     */
    @Transactional
    public void unfreeze(Long id) {
        WhInventory inv = inventoryRepository.selectById(id);
        if (inv == null) {
            throw new BusinessException("库存记录不存在");
        }
        inv.setStatus(1); // 恢复正常
        inventoryRepository.updateById(inv);
        log.info("解除冻结: id={}", id);
    }

    /**
     * 报损
     */
    @Transactional
    public void reportDamage(Long id, BigDecimal damageQty) {
        WhInventory inv = inventoryRepository.selectById(id);
        if (inv == null) {
            throw new BusinessException("库存记录不存在");
        }
        if (inv.getQuantity().compareTo(damageQty) < 0) {
            throw new BusinessException("报损数量不能大于库存数量");
        }
        inv.setQuantity(inv.getQuantity().subtract(damageQty));
        if (inv.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            inv.setStatus(3); // 报损
        }
        if (inv.getUnitPrice() != null) {
            inv.setTotalValue(inv.getQuantity().multiply(inv.getUnitPrice()));
        }
        inventoryRepository.updateById(inv);
        log.info("报损: id={}, qty={}", id, damageQty);
    }

    /**
     * 查询低库存预警（低于安全库存）
     */
    public List<Map<String, Object>> getLowStockAlerts(Long warehouseId) {
        LambdaQueryWrapper<WhInventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WhInventory::getStatus, 1); // 只查正常库存
        if (warehouseId != null) {
            wrapper.eq(WhInventory::getWarehouseId, warehouseId);
        }
        // 低于 10 个单位视为低库存
        wrapper.lt(WhInventory::getQuantity, new BigDecimal("10"));
        wrapper.orderByAsc(WhInventory::getQuantity);

        return inventoryRepository.selectList(wrapper).stream().map(inv -> {
            Map<String, Object> m = toMap(inv);
            WhWarehouse wh = warehouseRepository.selectById(inv.getWarehouseId());
            if (wh != null) {
                m.put("warehouseName", wh.getName());
            }
            return m;
        }).toList();
    }

    private Map<String, Object> toMap(WhInventory inv) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", inv.getId());
        m.put("warehouseId", inv.getWarehouseId());
        m.put("locationId", inv.getLocationId());
        m.put("sku", inv.getSku());
        m.put("goodsName", inv.getGoodsName());
        m.put("quantity", inv.getQuantity());
        m.put("unit", inv.getUnit());
        m.put("unitPrice", inv.getUnitPrice());
        m.put("totalValue", inv.getTotalValue());
        m.put("batchNo", inv.getBatchNo());
        m.put("productionDate", inv.getProductionDate());
        m.put("expiryDate", inv.getExpiryDate());
        m.put("status", inv.getStatus());
        m.put("statusName", getStatusName(inv.getStatus()));
        return m;
    }

    private String getStatusName(Integer status) {
        return switch (status) {
            case 1 -> "正常";
            case 2 -> "冻结";
            case 3 -> "报损";
            default -> "未知";
        };
    }
}
