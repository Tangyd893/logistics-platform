package com.logistics.warehouse.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.logistics.common.exception.BusinessException;
import com.logistics.warehouse.domain.dto.InboundOrderCreateRequest;
import com.logistics.warehouse.domain.entity.WhInboundItem;
import com.logistics.warehouse.domain.entity.WhInboundOrder;
import com.logistics.warehouse.domain.entity.WhInventory;
import com.logistics.warehouse.domain.entity.WhWarehouse;
import com.logistics.warehouse.domain.vo.InboundOrderVO;
import com.logistics.warehouse.domain.vo.InboundOrderVO.InboundItemVO;
import com.logistics.warehouse.repository.WhInboundItemRepository;
import com.logistics.warehouse.repository.WhInboundOrderRepository;
import com.logistics.warehouse.repository.WhInventoryRepository;
import com.logistics.warehouse.repository.WhWarehouseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class InboundOrderService {

    private static final Logger log = LoggerFactory.getLogger(InboundOrderService.class);
    private static final DateTimeFormatter ORDER_NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final WhInboundOrderRepository inboundOrderRepository;
    private final WhInboundItemRepository inboundItemRepository;
    private final WhInventoryRepository inventoryRepository;
    private final WhWarehouseRepository warehouseRepository;

    @Autowired
    public InboundOrderService(WhInboundOrderRepository inboundOrderRepository,
                               WhInboundItemRepository inboundItemRepository,
                               WhInventoryRepository inventoryRepository,
                               WhWarehouseRepository warehouseRepository) {
        this.inboundOrderRepository = inboundOrderRepository;
        this.inboundItemRepository = inboundItemRepository;
        this.inventoryRepository = inventoryRepository;
        this.warehouseRepository = warehouseRepository;
    }

    /**
     * 分页查询入库单
     */
    public com.logistics.common.dto.PageDTO<InboundOrderVO> page(Long warehouseId, String keyword,
                                                                   Integer status, int page, int size) {
        LambdaQueryWrapper<WhInboundOrder> wrapper = new LambdaQueryWrapper<>();
        if (warehouseId != null) {
            wrapper.eq(WhInboundOrder::getWarehouseId, warehouseId);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(WhInboundOrder::getOrderNo, keyword)
                    .or().like(WhInboundOrder::getSupplierName, keyword));
        }
        if (status != null) {
            wrapper.eq(WhInboundOrder::getStatus, status);
        }
        wrapper.orderByDesc(WhInboundOrder::getCreatedAt);

        Page<WhInboundOrder> result = inboundOrderRepository.selectPage(new Page<>(page, size), wrapper);
        List<InboundOrderVO> voList = result.getRecords().stream().map(this::toVO).toList();
        return com.logistics.common.dto.PageDTO.of(voList, result.getTotal(), page, size);
    }

    /**
     * 根据ID查询入库单（含明细）
     */
    public InboundOrderVO getById(Long id) {
        WhInboundOrder order = inboundOrderRepository.selectById(id);
        if (order == null) {
            throw new BusinessException("入库单不存在");
        }
        return toVO(order);
    }

    /**
     * 创建入库单
     */
    @Transactional
    public InboundOrderVO create(InboundOrderCreateRequest request) {
        // 校验仓库
        WhWarehouse warehouse = warehouseRepository.selectById(request.getWarehouseId());
        if (warehouse == null) {
            throw new BusinessException("仓库不存在");
        }

        // 生成单号
        String orderNo = generateOrderNo();

        WhInboundOrder order = new WhInboundOrder();
        order.setOrderNo(orderNo);
        order.setWarehouseId(request.getWarehouseId());
        order.setSupplierName(request.getSupplierName());
        order.setExpectedArrivalTime(request.getExpectedArrivalTime());
        order.setInboundType(request.getInboundType());
        order.setStatus(10); // 待确认
        order.setRemark(request.getRemark());

        inboundOrderRepository.insert(order);

        // 保存明细
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (InboundOrderCreateRequest.ItemRequest itemReq : request.getItems()) {
                WhInboundItem item = new WhInboundItem();
                item.setInboundId(order.getId());
                item.setSku(itemReq.getSku());
                item.setGoodsName(itemReq.getGoodsName());
                item.setExpectedQty(itemReq.getExpectedQty());
                item.setActualQty(BigDecimal.ZERO);
                item.setUnit(itemReq.getUnit());
                item.setUnitPrice(itemReq.getUnitPrice());
                item.setBatchNo(itemReq.getBatchNo());
                item.setProductionDate(itemReq.getProductionDate());
                item.setExpiryDate(itemReq.getExpiryDate());
                item.setLocationId(itemReq.getLocationId());
                item.setStatus(10); // 待入库
                inboundItemRepository.insert(item);
            }
        }

        log.info("创建入库单: {}", orderNo);
        return toVO(order);
    }

    /**
     * 确认入库单
     */
    @Transactional
    public InboundOrderVO confirm(Long id) {
        WhInboundOrder order = inboundOrderRepository.selectById(id);
        if (order == null) {
            throw new BusinessException("入库单不存在");
        }
        if (order.getStatus() != 10) {
            throw new BusinessException("只有待确认状态的入库单可以确认");
        }
        order.setStatus(20);
        inboundOrderRepository.updateById(order);
        log.info("确认入库单: {}", order.getOrderNo());
        return toVO(order);
    }

    /**
     * 开始入库（实际入库操作）
     */
    @Transactional
    public InboundOrderVO startInbound(Long id, Long operatorId) {
        WhInboundOrder order = inboundOrderRepository.selectById(id);
        if (order == null) {
            throw new BusinessException("入库单不存在");
        }
        if (order.getStatus() != 20 && order.getStatus() != 30) {
            throw new BusinessException("入库单状态无法开始入库");
        }

        // 查询明细
        LambdaQueryWrapper<WhInboundItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(WhInboundItem::getInboundId, id);
        List<WhInboundItem> items = inboundItemRepository.selectList(itemWrapper);

        // 更新每个明细项的状态，增加实际入库数量，并更新库存
        for (WhInboundItem item : items) {
            if (item.getActualQty().compareTo(BigDecimal.ZERO) > 0) {
                // 更新库存
                updateInventoryOnInbound(order.getWarehouseId(), item);

                // 如果实际数量等于预期数量，标记为已完成
                if (item.getActualQty().compareTo(item.getExpectedQty()) >= 0) {
                    item.setStatus(30); // 已完成
                } else {
                    item.setStatus(20); // 部分入库
                }
                inboundItemRepository.updateById(item);
            }
        }

        // 更新订单状态
        order.setActualArrivalTime(LocalDateTime.now());
        order.setStatus(40); // 已完成
        inboundOrderRepository.updateById(order);

        // 更新仓库已用容量
        updateWarehouseUsedCapacity(order.getWarehouseId());

        log.info("入库完成: {}", order.getOrderNo());
        return toVO(order);
    }

    /**
     * 取消入库单
     */
    @Transactional
    public void cancel(Long id) {
        WhInboundOrder order = inboundOrderRepository.selectById(id);
        if (order == null) {
            throw new BusinessException("入库单不存在");
        }
        if (order.getStatus() == 40) {
            throw new BusinessException("已完成的入库单无法取消");
        }
        order.setStatus(50);
        inboundOrderRepository.updateById(order);
        log.info("取消入库单: {}", order.getOrderNo());
    }

    private void updateInventoryOnInbound(Long warehouseId, WhInboundItem item) {
        // 查询是否已有该仓库+SKU+批次的库存
        LambdaQueryWrapper<WhInventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WhInventory::getWarehouseId, warehouseId)
               .eq(WhInventory::getSku, item.getSku())
               .eq(WhInventory::getLocationId, item.getLocationId())
               .eq(WhInventory::getStatus, 1);

        List<WhInventory> existing = inventoryRepository.selectList(wrapper);

        if (!existing.isEmpty()) {
            // 累加库存
            WhInventory inv = existing.get(0);
            inv.setQuantity(inv.getQuantity().add(item.getActualQty()));
            if (inv.getTotalValue() != null && item.getUnitPrice() != null) {
                inv.setTotalValue(inv.getQuantity().multiply(item.getUnitPrice()));
            }
            inventoryRepository.updateById(inv);
        } else {
            // 新增库存记录
            WhInventory inv = new WhInventory();
            inv.setWarehouseId(warehouseId);
            inv.setLocationId(item.getLocationId());
            inv.setSku(item.getSku());
            inv.setGoodsName(item.getGoodsName());
            inv.setQuantity(item.getActualQty());
            inv.setUnit(item.getUnit());
            inv.setUnitPrice(item.getUnitPrice());
            if (item.getUnitPrice() != null) {
                inv.setTotalValue(item.getActualQty().multiply(item.getUnitPrice()));
            }
            inv.setBatchNo(item.getBatchNo());
            inv.setProductionDate(item.getProductionDate());
            inv.setExpiryDate(item.getExpiryDate());
            inv.setStatus(1);
            inventoryRepository.insert(inv);
        }
    }

    private void updateWarehouseUsedCapacity(Long warehouseId) {
        // 简单计算：从库存表中汇总该仓库的总数量
        LambdaQueryWrapper<WhInventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WhInventory::getWarehouseId, warehouseId)
               .eq(WhInventory::getStatus, 1);
        List<WhInventory> inventories = inventoryRepository.selectList(wrapper);

        BigDecimal totalUsed = inventories.stream()
                .map(inv -> inv.getQuantity() != null ? inv.getQuantity() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 按每单位占用 0.001 立方米简化估算
        BigDecimal estimatedVolume = totalUsed.multiply(new BigDecimal("0.001"));

        WhWarehouse warehouse = warehouseRepository.selectById(warehouseId);
        if (warehouse != null) {
            warehouse.setUsedCapacity(estimatedVolume);
            warehouseRepository.updateById(warehouse);
        }
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(ORDER_NO_FORMAT);
        AtomicLong seq = new AtomicLong((long) (Math.random() * 1000));
        return "IN" + timestamp + String.format("%03d", seq.get());
    }

    private InboundOrderVO toVO(WhInboundOrder order) {
        InboundOrderVO vo = new InboundOrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setWarehouseId(order.getWarehouseId());
        vo.setSupplierName(order.getSupplierName());
        vo.setExpectedArrivalTime(order.getExpectedArrivalTime());
        vo.setActualArrivalTime(order.getActualArrivalTime());
        vo.setInboundType(order.getInboundType());
        vo.setInboundTypeName(getInboundTypeName(order.getInboundType()));
        vo.setStatus(order.getStatus());
        vo.setStatusName(getStatusName(order.getStatus()));
        vo.setRemark(order.getRemark());
        vo.setOperator(order.getOperator());
        vo.setCreatedAt(order.getCreatedAt());

        // 查询仓库名
        WhWarehouse warehouse = warehouseRepository.selectById(order.getWarehouseId());
        if (warehouse != null) {
            vo.setWarehouseName(warehouse.getName());
        }

        // 查询明细
        LambdaQueryWrapper<WhInboundItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(WhInboundItem::getInboundId, order.getId());
        List<WhInboundItem> items = inboundItemRepository.selectList(itemWrapper);
        vo.setItems(items.stream().map(this::toItemVO).toList());

        return vo;
    }

    private InboundItemVO toItemVO(WhInboundItem item) {
        InboundItemVO vo = new InboundItemVO();
        vo.setId(item.getId());
        vo.setSku(item.getSku());
        vo.setGoodsName(item.getGoodsName());
        vo.setExpectedQty(item.getExpectedQty());
        vo.setActualQty(item.getActualQty());
        vo.setUnit(item.getUnit());
        vo.setUnitPrice(item.getUnitPrice());
        vo.setBatchNo(item.getBatchNo());
        vo.setProductionDate(item.getProductionDate());
        vo.setExpiryDate(item.getExpiryDate());
        vo.setLocationId(item.getLocationId());
        vo.setStatus(item.getStatus());
        vo.setStatusName(getItemStatusName(item.getStatus()));
        return vo;
    }

    private String getInboundTypeName(String type) {
        return switch (type) {
            case "PURCHASE" -> "采购入库";
            case "RETURN" -> "退货入库";
            case "TRANSFER" -> "调拨入库";
            default -> type;
        };
    }

    private String getStatusName(Integer status) {
        return switch (status) {
            case 10 -> "待确认";
            case 20 -> "已确认";
            case 30 -> "入库中";
            case 40 -> "已完成";
            case 50 -> "已取消";
            default -> "未知";
        };
    }

    private String getItemStatusName(Integer status) {
        return switch (status) {
            case 10 -> "待入库";
            case 20 -> "部分入库";
            case 30 -> "已完成";
            default -> "未知";
        };
    }
}
