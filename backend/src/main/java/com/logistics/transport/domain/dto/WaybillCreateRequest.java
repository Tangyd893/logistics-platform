package com.logistics.transport.domain.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 运单创建请求
 */
public class WaybillCreateRequest {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    @NotNull(message = "司机ID不能为空")
    private Long driverId;

    @NotNull(message = "车辆ID不能为空")
    private Long vehicleId;

    private LocalDateTime planPickupTime;
    private LocalDateTime planDeliveryTime;

    private String fromAddress;
    private String toAddress;


    public String getFromAddress() { return fromAddress; }
    public void setFromAddress(String fromAddress) { this.fromAddress = fromAddress; }
    public String getToAddress() { return toAddress; }
    public void setToAddress(String toAddress) { this.toAddress = toAddress; }

    public WaybillCreateRequest() {}

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }

    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }

    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }

    public LocalDateTime getPlanPickupTime() { return planPickupTime; }
    public void setPlanPickupTime(LocalDateTime planPickupTime) { this.planPickupTime = planPickupTime; }

    public LocalDateTime getPlanDeliveryTime() { return planDeliveryTime; }
    public void setPlanDeliveryTime(LocalDateTime planDeliveryTime) { this.planDeliveryTime = planDeliveryTime; }
}
