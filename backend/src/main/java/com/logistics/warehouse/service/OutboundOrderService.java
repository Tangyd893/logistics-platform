package com.logistics.warehouse.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.logistics.common.exception.BusinessException;
import com.logistics.warehouse.domain.dto.OutboundOrderCreateRequest;
import com.logistics.warehouse.domain.entity.*;
import com.logistics.warehouse.domain.vo.OutboundOrderVO;
import com.logistics.warehouse.domain.vo.OutboundOrderVO.OutboundItemVO;
import com.logistics.warehouse.repository.*;
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
public class OutboundOrderService {

    private static final Logger log = LoggerFactory.getLogger(OutboundOrderService.class);
    private static final DateTimeFormatter ORDER_NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final WhOutboundOrderRepository outboundOrderRepository;
    private final WhOutboundItemRepository outboundItemRepository;
    private final WhInventoryRepository inventoryRepository;
    private final WhWarehouseRepository warehouseRepository;

    @Autowired
    public OutboundOrderService(WhOutboundOrderRepository outboundOrderRepository,
                                 WhOutboundItemRepository outboundItemRepository,
                                 WhInventoryRepository inventoryRepository,
                                 WhWarehouseRepository warehouseRepository) {
        this.outboundOrderRepository = outboundOrderRepository;
        this.outboundItemRepository = outboundItemRepository;
        this.inventoryRepository = inventoryRepository;
        this.warehouseRepository = warehouseRepository;
    }

    /**
     * 分页查询出库单
     */
    public com.logistics.common.dto.PageDTO<OutboundOrderVO> page(Long warehouseId, String keyword,
                                                                   Integer status, int page, int size) {
        LambdaQueryWrapper<WhOutboundOrder> wrapper = new LambdaQueryWrapper<>();
        if (warehouseId != null) {
            wrapper.eq(WhOutboundOrder::getWarehouseId, warehouseId);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(WhOutboundOrder::getOrderNo, keyword)
                    .or().like(WhOutboundOrder::getCustomerName, keyword));
        }
        if (status != null) {
            wrapper.eq(WhOutboundOrder::getStatus, status);
        }
        wrapper.orderByDesc(WhOutboundOrder::getCreatedAt);

        Page<WhOutboundOrder> result = outboundOrderRepository.selectPage(new Page<>(page, size), wrapper);
        List<OutboundOrderVO> voList = result.getRecords().stream().map(this::toVO).toList();
        return com.logistics.common.dto.PageDTO.of(voList, result.getTotal(), page, size);
    }

    /**
     * 根据ID查询出库单
     */
    public OutboundOrderVO getById(Long id) {
        WhOutboundOrder order = outboundOrderRepository.selectById(id);
        if (order == null) {
            throw new BusinessException("出库单不存在");
        }
        return toVO(order);
    }

    /**
     * 创建出库单
     */
    @Transactional
    public OutboundOrderVO create(OutboundOrderCreateRequest request) {
        // 校验仓库
        WhWarehouse warehouse = warehouseRepository.selectById(request.getWarehouseId());
        if (warehouse == null) {
            throw new BusinessException("仓库不存在");
        }

        String orderNo = generateOrderNo();

        WhOutboundOrder order = new WhOutboundOrder();
        order.setOrderNo(orderNo);
        order.setWarehouseId(request.getWarehouseId());
        order.setCustomerName(request.getCustomerName());
        order.setCustomerAddress(request.getCustomerAddress());
        order.setCustomerPhone(request.getCustomerPhone());
        order.setOutboundType(request.getOutboundType());
        order.setStatus(10); // 待确认
        order.setRemark(request.getRemark());

        outboundOrderRepository.insert(order);

        // 保存明细
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (OutboundOrderCreateRequest.ItemRequest itemReq : request.getItems()) {
                WhOutboundItem item = new WhOutboundItem();
                item.setOutboundId(order.getId());
                item.setSku(itemReq.getSku());
                item.setGoodsName(itemReq.getGoodsName());
                item.setOrderQty(itemReq.getOrderQty());
                item.setPickedQty(BigDecimal.ZERO);
                item.setUnit(itemReq.getUnit());
                item.setBatchNo(itemReq.getBatchNo());
                item.setLocationId(itemReq.getLocationId());
                item.setStatus(10); // 待拣货
                outboundItemRepository.insert(item);
            }
        }

        log.info("创建出库单: {}", orderNo);
        return toVO(order);
    }

    /**
     * 确认出库单
     */
    @Transactional
    public OutboundOrderVO confirm(Long id) {
        WhOutboundOrder order = outboundOrderRepository.selectById(id);
        if (order == null) {
            throw new BusinessException("出库单不存在");
        }
        if (order.getStatus() != 10) {
            throw new BusinessException("只有待确认状态的出库单可以确认");
        }
        order.setStatus(20); // 待拣货
        outboundOrderRepository.updateById(order);
        log.info("确认出库单: {}", order.getOrderNo());
        return toVO(order);
    }

    /**
     * 开始拣货
     */
    @Transactional
    public OutboundOrderVO startPicking(Long id) {
        WhOutboundOrder order = outboundOrderRepository.selectById(id);
        if (order == null) {
            throw new BusinessException("出库单不存在");
        }
        if (order.getStatus() != 20) {
            throw new BusinessException("只有待拣货状态的出库单可以开始拣货");
        }
        order.setStatus(30); // 拣货中
        outboundOrderRepository.updateById(order);
        log.info("开始拣货: {}", order.getOrderNo());
        return toVO(order);
    }

    /**
     * 完成出库
     */
    @Transactional
    public OutboundOrderVO completeOutbound(Long id) {
        WhOutboundOrder order = outboundOrderRepository.selectById(id);
        if (order == null) {
            throw new BusinessException("出库单不存在");
        }
        if (order.getStatus() != 30) {
            throw new BusinessException("只有拣货中的出库单可以完成出库");
        }

        // 查询明细并扣减库存
        LambdaQueryWrapper<WhOutboundItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(WhOutboundItem::getOutboundId, id);
        List<WhOutboundItem> items = outboundItemRepository.selectList(itemWrapper);

        for (WhOutboundItem item : items) {
            // 扣减库存
            deductInventory(order.getWarehouseId(), item);

            // 更新拣货状态
            if (item.getPickedQty().compareTo(item.getOrderQty()) >= 0) {
                item.setStatus(30); // 已完成
            } else if (item.getPickedQty().compareTo(BigDecimal.ZERO) > 0) {
                item.setStatus(20); // 部分拣货
            }
            outboundItemRepository.updateById(item);
        }

        order.setStatus(40); // 已出库
        outboundOrderRepository.updateById(order);

        log.info("出库完成: {}", order.getOrderNo());
        return toVO(order);
    }

    /**
     * 取消出库单
     */
    @Transactional
    public void cancel(Long id) {
        WhOutboundOrder order = outboundOrderRepository.selectById(id);
        if (order == null) {
            throw new BusinessException("出库单不存在");
        }
        if (order.getStatus() == 40) {
            throw new BusinessException("已出库的单据无法取消");
        }
        order.setStatus(50);
        outboundOrderRepository.updateById(order);
        log.info("取消出库单: {}", order.getOrderNo());
    }

    private void deductInventory(Long warehouseId, WhOutboundItem item) {
        LambdaQueryWrapper<WhInventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WhInventory::getWarehouseId, warehouseId)
               .eq(WhInventory::getSku, item.getSku())
               .eq(WhInventory::getStatus, 1);

        List<WhInventory> inventories = inventoryRepository.selectList(wrapper);

        BigDecimal remaining = item.getPickedQty();

        for (WhInventory inv : inventories) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;

            if (inv.getQuantity().compareTo(remaining) >= 0) {
                // 库存足够，直接扣减
                inv.setQuantity(inv.getQuantity().subtract(remaining));
                remaining = BigDecimal.ZERO;
            } else {
                // 库存不足，扣减全部
                remaining = remaining.subtract(inv.getQuantity());
                inv.setQuantity(BigDecimal.ZERO);
            }

            if (inv.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                inv.setStatus(3); // 报损
            }

            if (inv.getUnitPrice() != null) {
                inv.setTotalValue(inv.getQuantity().multiply(inv.getUnitPrice()));
            }
            inventoryRepository.updateById(inv);
        }
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(ORDER_NO_FORMAT);
        AtomicLong seq = new AtomicLong((long) (Math.random() * 1000));
        return "OUT" + timestamp + String.format("%03d", seq.get());
    }

    private OutboundOrderVO toVO(WhOutboundOrder order) {
        OutboundOrderVO vo = new OutboundOrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setWarehouseId(order.getWarehouseId());
        vo.setCustomerName(order.getCustomerName());
        vo.setCustomerAddress(order.getCustomerAddress());
        vo.setCustomerPhone(order.getCustomerPhone());
        vo.setOutboundType(order.getOutboundType());
        vo.setOutboundTypeName(getOutboundTypeName(order.getOutboundType()));
        vo.setStatus(order.getStatus());
        vo.setStatusName(getStatusName(order.getStatus()));
        vo.setRemark(order.getRemark());
        vo.setOperator(order.getOperator());
        vo.setCreatedAt(order.getCreatedAt());

        WhWarehouse warehouse = warehouseRepository.selectById(order.getWarehouseId());
        if (warehouse != null) {
            vo.setWarehouseName(warehouse.getName());
        }

        LambdaQueryWrapper<WhOutboundItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(WhOutboundItem::getOutboundId, order.getId());
        List<WhOutboundItem> items = outboundItemRepository.selectList(itemWrapper);
        vo.setItems(items.stream().map(this::toItemVO).toList());

        return vo;
    }

    private OutboundItemVO toItemVO(WhOutboundItem item) {
        OutboundItemVO vo = new OutboundItemVO();
        vo.setId(item.getId());
        vo.setSku(item.getSku());
        vo.setGoodsName(item.getGoodsName());
        vo.setOrderQty(item.getOrderQty());
        vo.setPickedQty(item.getPickedQty());
        vo.setUnit(item.getUnit());
        vo.setBatchNo(item.getBatchNo());
        vo.setLocationId(item.getLocationId());
        vo.setInventoryId(item.getInventoryId());
        vo.setStatus(item.getStatus());
        vo.setStatusName(getItemStatusName(item.getStatus()));
        return vo;
    }

    private String getOutboundTypeName(String type) {
        return switch (type) {
            case "SALE" -> "销售出库";
            case "RETURN" -> "退料出库";
            case "TRANSFER" -> "调拨出库";
            default -> type;
        };
    }

    private String getStatusName(Integer status) {
        return switch (status) {
            case 10 -> "待确认";
            case 20 -> "待拣货";
            case 30 -> "拣货中";
            case 40 -> "已出库";
            case 50 -> "已取消";
            default -> "未知";
        };
    }

    private String getItemStatusName(Integer status) {
        return switch (status) {
            case 10 -> "待拣货";
            case 20 -> "部分拣货";
            case 30 -> "已完成";
            default -> "未知";
        };
    }
}
