package com.logistics.transport.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.logistics.common.dto.PageDTO;
import com.logistics.common.exception.BusinessException;
import com.logistics.order.domain.entity.OOrder;
import com.logistics.order.repository.OOrderRepository;
import com.logistics.transport.domain.dto.WaybillCreateRequest;
import com.logistics.transport.domain.entity.TDriver;
import com.logistics.transport.domain.entity.TTracking;
import com.logistics.transport.domain.entity.TVehicle;
import com.logistics.transport.domain.entity.TWaybill;
import com.logistics.transport.domain.vo.WaybillVO;
import com.logistics.transport.domain.vo.TrackingVO;
import com.logistics.transport.repository.TDriverRepository;
import com.logistics.transport.repository.TTrackingRepository;
import com.logistics.transport.repository.TVehicleRepository;
import com.logistics.transport.repository.TWaybillRepository;
import com.logistics.warehouse.domain.entity.WhWarehouse;
import com.logistics.warehouse.repository.WhWarehouseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WaybillService {

    private static final Logger log = LoggerFactory.getLogger(WaybillService.class);
    private static final DateTimeFormatter WAYBILL_NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final TWaybillRepository waybillRepository;
    private final TDriverRepository driverRepository;
    private final TVehicleRepository vehicleRepository;
    private final TTrackingRepository trackingRepository;
    private final OOrderRepository orderRepository;
    private final WhWarehouseRepository warehouseRepository;

    @Autowired
    public WaybillService(TWaybillRepository waybillRepository,
                         TDriverRepository driverRepository,
                         TVehicleRepository vehicleRepository,
                         TTrackingRepository trackingRepository,
                         OOrderRepository orderRepository,
                         WhWarehouseRepository warehouseRepository) {
        this.waybillRepository = waybillRepository;
        this.driverRepository = driverRepository;
        this.vehicleRepository = vehicleRepository;
        this.trackingRepository = trackingRepository;
        this.orderRepository = orderRepository;
        this.warehouseRepository = warehouseRepository;
    }

    /**
     * 分页查询运单
     */
    public PageDTO<WaybillVO> page(Long warehouseId, Long driverId, Integer status,
                                    String waybillNo, int page, int size) {
        LambdaQueryWrapper<TWaybill> wrapper = new LambdaQueryWrapper<>();
        if (warehouseId != null) {
            wrapper.eq(TWaybill::getWarehouseId, warehouseId);
        }
        if (driverId != null) {
            wrapper.eq(TWaybill::getDriverId, driverId);
        }
        if (status != null) {
            wrapper.eq(TWaybill::getStatus, status);
        }
        if (waybillNo != null && !waybillNo.isBlank()) {
            wrapper.like(TWaybill::getWaybillNo, waybillNo);
        }
        wrapper.orderByDesc(TWaybill::getCreatedAt);

        Page<TWaybill> result = waybillRepository.selectPage(new Page<>(page, size), wrapper);
        List<WaybillVO> voList = result.getRecords().stream().map(this::toVO).toList();
        return PageDTO.of(voList, result.getTotal(), page, size);
    }

    /**
     * 根据ID查询运单
     */
    public WaybillVO getById(Long id) {
        TWaybill waybill = waybillRepository.selectById(id);
        if (waybill == null) {
            throw new BusinessException("运单不存在");
        }
        return toVO(waybill);
    }

    /**
     * 根据运单号查询
     */
    public WaybillVO getByWaybillNo(String waybillNo) {
        LambdaQueryWrapper<TWaybill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TWaybill::getWaybillNo, waybillNo);
        TWaybill waybill = waybillRepository.selectOne(wrapper);
        if (waybill == null) {
            throw new BusinessException("运单不存在");
        }
        return toVO(waybill);
    }

    /**
     * 创建运单
     */
    @Transactional
    public WaybillVO create(WaybillCreateRequest request) {
        // 校验订单存在
        OOrder order = orderRepository.selectById(request.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() < 30) {
            throw new BusinessException("订单状态未到达可配送阶段（需已入库）");
        }

        // 校验司机
        TDriver driver = driverRepository.selectById(request.getDriverId());
        if (driver == null) {
            throw new BusinessException("司机不存在");
        }
        if (driver.getStatus() == 2) {
            throw new BusinessException("司机正在配送中");
        }

        // 校验车辆
        TVehicle vehicle = vehicleRepository.selectById(request.getVehicleId());
        if (vehicle == null) {
            throw new BusinessException("车辆不存在");
        }
        if (vehicle.getStatus() != 1) {
            throw new BusinessException("车辆不可用");
        }

        // 生成运单号
        String waybillNo = generateWaybillNo();

        TWaybill waybill = new TWaybill();
        waybill.setWaybillNo(waybillNo);
        waybill.setOrderId(request.getOrderId());
        waybill.setWarehouseId(request.getWarehouseId());
        waybill.setDriverId(request.getDriverId());
        waybill.setVehicleId(request.getVehicleId());
        waybill.setPlanPickupTime(request.getPlanPickupTime());
        waybill.setPlanDeliveryTime(request.getPlanDeliveryTime());
        waybill.setFromAddress(request.getFromAddress());
        waybill.setToAddress(request.getToAddress());
        waybill.setStatus(1); // 待提货
        waybillRepository.insert(waybill);

        // 更新订单状态为已发货(40)
        order.setStatus(40);
        orderRepository.updateById(order);

        // 更新司机和车辆状态
        driver.setStatus(2);
        driverRepository.updateById(driver);
        vehicle.setStatus(2);
        vehicleRepository.updateById(vehicle);

        // 记录轨迹
        addTracking(waybill.getId(), 1, "运单已创建，等待提货", null, null, null);

        log.info("创建运单: {}", waybillNo);
        return toVO(waybill);
    }

    /**
     * 提货确认
     */
    @Transactional
    public WaybillVO confirmPickup(Long id) {
        TWaybill waybill = waybillRepository.selectById(id);
        if (waybill == null) {
            throw new BusinessException("运单不存在");
        }
        if (waybill.getStatus() != 1) {
            throw new BusinessException("只有待提货状态的运单可以提货");
        }
        waybill.setStatus(2); // 配送中
        waybill.setActualPickupTime(LocalDateTime.now());
        waybillRepository.updateById(waybill);

        // 记录轨迹
        addTracking(waybill.getId(), 2, "已提货，配送中", null, null, null);

        // 更新订单状态为运输中(50)
        updateOrderStatus(waybill.getOrderId(), 50, "已提货");

        log.info("提货确认: {}", waybill.getWaybillNo());
        return toVO(waybill);
    }

    /**
     * 确认送达
     */
    @Transactional
    public WaybillVO confirmDelivery(Long id) {
        TWaybill waybill = waybillRepository.selectById(id);
        if (waybill == null) {
            throw new BusinessException("运单不存在");
        }
        if (waybill.getStatus() != 2) {
            throw new BusinessException("只有配送中的运单可以确认送达");
        }
        waybill.setStatus(3); // 已送达
        waybill.setActualDeliveryTime(LocalDateTime.now());
        waybillRepository.updateById(waybill);

        // 释放司机和车辆
        releaseDriverAndVehicle(waybill.getDriverId(), waybill.getVehicleId());

        // 记录轨迹
        addTracking(waybill.getId(), 3, "已送达，配送完成", null, null, null);

        // 更新订单状态为已送达(60)
        updateOrderStatus(waybill.getOrderId(), 60, "已送达");

        log.info("确认送达: {}", waybill.getWaybillNo());
        return toVO(waybill);
    }

    /**
     * 拒收
     */
    @Transactional
    public WaybillVO reject(Long id, String reason) {
        TWaybill waybill = waybillRepository.selectById(id);
        if (waybill == null) {
            throw new BusinessException("运单不存在");
        }
        if (waybill.getStatus() == 3 || waybill.getStatus() == 4) {
            throw new BusinessException("运单已结束，无法拒收");
        }
        waybill.setStatus(4); // 拒收
        waybillRepository.updateById(waybill);

        // 释放司机和车辆
        releaseDriverAndVehicle(waybill.getDriverId(), waybill.getVehicleId());

        // 记录轨迹
        addTracking(waybill.getId(), 4, "拒收: " + (reason != null ? reason : ""), null, null, null);

        // 更新订单状态为已送达(60)
        updateOrderStatus(waybill.getOrderId(), 60, "拒收");

        log.info("运单拒收: {}, reason={}", waybill.getWaybillNo(), reason);
        return toVO(waybill);
    }

    /**
     * 取消运单
     */
    @Transactional
    public void cancel(Long id) {
        TWaybill waybill = waybillRepository.selectById(id);
        if (waybill == null) {
            throw new BusinessException("运单不存在");
        }
        if (waybill.getStatus() == 3 || waybill.getStatus() == 4) {
            throw new BusinessException("运单已结束，无法取消");
        }
        // 释放司机和车辆
        releaseDriverAndVehicle(waybill.getDriverId(), waybill.getVehicleId());

        // 恢复订单状态为已发货(40)
        updateOrderStatus(waybill.getOrderId(), 40, "运单取消");

        waybill.setStatus(4);
        waybillRepository.updateById(waybill);
        log.info("取消运单: {}", waybill.getWaybillNo());
    }

    /**
     * 查询运单轨迹
     */
    public List<TrackingVO> getTrackings(Long waybillId) {
        LambdaQueryWrapper<TTracking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TTracking::getWaybillId, waybillId)
               .orderByAsc(TTracking::getOperateTime);
        return trackingRepository.selectList(wrapper).stream().map(this::toTrackingVO).toList();
    }

    /**
     * 添加轨迹
     */
    @Transactional
    public TrackingVO addTracking(Long waybillId, Integer status, String description,
                                  String location, java.math.BigDecimal lat, java.math.BigDecimal lng) {
        TTracking tracking = new TTracking();
        tracking.setWaybillId(waybillId);
        tracking.setStatus(status);
        tracking.setDescription(description);
        tracking.setLocation(location);
        tracking.setLatitude(lat);
        tracking.setLongitude(lng);
        tracking.setOperateTime(LocalDateTime.now());
        trackingRepository.insert(tracking);
        return toTrackingVO(tracking);
    }

    // ==================== Private Methods ====================

    private void releaseDriverAndVehicle(Long driverId, Long vehicleId) {
        if (driverId != null) {
            TDriver driver = driverRepository.selectById(driverId);
            if (driver != null) {
                driver.setStatus(1);
                driverRepository.updateById(driver);
            }
        }
        if (vehicleId != null) {
            TVehicle vehicle = vehicleRepository.selectById(vehicleId);
            if (vehicle != null) {
                vehicle.setStatus(1);
                vehicleRepository.updateById(vehicle);
            }
        }
    }

    private void updateOrderStatus(Long orderId, Integer status, String remark) {
        OOrder order = orderRepository.selectById(orderId);
        if (order != null) {
            order.setStatus(status);
            orderRepository.updateById(order);
        }
    }

    private String generateWaybillNo() {
        String timestamp = LocalDateTime.now().format(WAYBILL_NO_FORMAT);
        long seq = (long) (Math.random() * 10000);
        return "WB" + timestamp + String.format("%04d", seq);
    }

    private WaybillVO toVO(TWaybill waybill) {
        WaybillVO vo = new WaybillVO();
        vo.setId(waybill.getId());
        vo.setWaybillNo(waybill.getWaybillNo());
        vo.setOrderId(waybill.getOrderId());
        vo.setWarehouseId(waybill.getWarehouseId());
        vo.setDriverId(waybill.getDriverId());
        vo.setVehicleId(waybill.getVehicleId());
        vo.setPlanPickupTime(waybill.getPlanPickupTime());
        vo.setPlanDeliveryTime(waybill.getPlanDeliveryTime());
        vo.setActualPickupTime(waybill.getActualPickupTime());
        vo.setActualDeliveryTime(waybill.getActualDeliveryTime());
        vo.setFromAddress(waybill.getFromAddress());
        vo.setToAddress(waybill.getToAddress());
        vo.setStatus(waybill.getStatus());
        vo.setStatusName(getStatusName(waybill.getStatus()));
        vo.setCreatedAt(waybill.getCreatedAt());

        // 关联订单号
        if (waybill.getOrderId() != null) {
            OOrder order = orderRepository.selectById(waybill.getOrderId());
            if (order != null) {
                vo.setOrderNo(order.getOrderNo());
            }
        }

        // 仓库名
        if (waybill.getWarehouseId() != null) {
            WhWarehouse wh = warehouseRepository.selectById(waybill.getWarehouseId());
            if (wh != null) {
                vo.setWarehouseName(wh.getName());
            }
        }

        // 司机信息
        if (waybill.getDriverId() != null) {
            TDriver driver = driverRepository.selectById(waybill.getDriverId());
            if (driver != null) {
                vo.setDriverName(driver.getName());
                vo.setDriverPhone(driver.getPhone());
            }
        }

        // 车辆信息
        if (waybill.getVehicleId() != null) {
            TVehicle vehicle = vehicleRepository.selectById(waybill.getVehicleId());
            if (vehicle != null) {
                vo.setVehiclePlateNo(vehicle.getPlateNo());
            }
        }

        // 轨迹列表
        LambdaQueryWrapper<TTracking> trackWrapper = new LambdaQueryWrapper<>();
        trackWrapper.eq(TTracking::getWaybillId, waybill.getId())
                   .orderByAsc(TTracking::getOperateTime);
        vo.setTrackings(trackingRepository.selectList(trackWrapper)
                .stream().map(this::toTrackingVO).toList());

        return vo;
    }

    private TrackingVO toTrackingVO(TTracking tracking) {
        TrackingVO vo = new TrackingVO();
        vo.setId(tracking.getId());
        vo.setWaybillId(tracking.getWaybillId());
        vo.setStatus(tracking.getStatus());
        vo.setStatusName(getStatusName(tracking.getStatus()));
        vo.setLocation(tracking.getLocation());
        vo.setLatitude(tracking.getLatitude());
        vo.setLongitude(tracking.getLongitude());
        vo.setDescription(tracking.getDescription());
        vo.setOperateTime(tracking.getOperateTime());
        return vo;
    }

    private String getStatusName(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 1 -> "待提货";
            case 2 -> "配送中";
            case 3 -> "已送达";
            case 4 -> "拒收/取消";
            default -> "未知";
        };
    }
}
