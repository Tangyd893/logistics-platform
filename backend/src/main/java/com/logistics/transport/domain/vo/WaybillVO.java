package com.logistics.transport.domain.vo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 运单视图对象
 */
public class WaybillVO {

    private Long id;
    private String waybillNo;
    private Long orderId;
    private String orderNo;
    private Long warehouseId;
    private String warehouseName;
    private Long driverId;
    private String driverName;
    private String driverPhone;
    private Long vehicleId;
    private String vehiclePlateNo;
    private LocalDateTime planPickupTime;
    private LocalDateTime planDeliveryTime;
    private LocalDateTime actualPickupTime;
    private LocalDateTime actualDeliveryTime;
    private Integer status;
    private String statusName;
    private String fromAddress;
    private String toAddress;
    private List<TrackingVO> trackings;
    private LocalDateTime createdAt;

    public WaybillVO() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getWaybillNo() { return waybillNo; }
    public void setWaybillNo(String waybillNo) { this.waybillNo = waybillNo; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }

    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }

    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    public String getDriverPhone() { return driverPhone; }
    public void setDriverPhone(String driverPhone) { this.driverPhone = driverPhone; }

    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }

    public String getVehiclePlateNo() { return vehiclePlateNo; }
    public void setVehiclePlateNo(String vehiclePlateNo) { this.vehiclePlateNo = vehiclePlateNo; }

    public LocalDateTime getPlanPickupTime() { return planPickupTime; }
    public void setPlanPickupTime(LocalDateTime planPickupTime) { this.planPickupTime = planPickupTime; }

    public LocalDateTime getPlanDeliveryTime() { return planDeliveryTime; }
    public void setPlanDeliveryTime(LocalDateTime planDeliveryTime) { this.planDeliveryTime = planDeliveryTime; }

    public LocalDateTime getActualPickupTime() { return actualPickupTime; }
    public void setActualPickupTime(LocalDateTime actualPickupTime) { this.actualPickupTime = actualPickupTime; }

    public LocalDateTime getActualDeliveryTime() { return actualDeliveryTime; }
    public void setActualDeliveryTime(LocalDateTime actualDeliveryTime) { this.actualDeliveryTime = actualDeliveryTime; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getStatusName() { return statusName; }
    public void setStatusName(String statusName) { this.statusName = statusName; }

    public String getFromAddress() { return fromAddress; }
    public void setFromAddress(String fromAddress) { this.fromAddress = fromAddress; }
    public String getToAddress() { return toAddress; }
    public void setToAddress(String toAddress) { this.toAddress = toAddress; }

    public List<TrackingVO> getTrackings() { return trackings; }
    public void setTrackings(List<TrackingVO> trackings) { this.trackings = trackings; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
