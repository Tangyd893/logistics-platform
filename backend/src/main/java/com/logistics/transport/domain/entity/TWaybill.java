package com.logistics.transport.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 运单
 */
@TableName("t_waybill")
public class TWaybill {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 运单号 */
    private String waybillNo;

    /** 关联订单ID */
    private Long orderId;

    /** 仓库ID */
    private Long warehouseId;

    /** 司机ID */
    private Long driverId;

    /** 车辆ID */
    private Long vehicleId;

    /** 计划提货时间 */
    private LocalDateTime planPickupTime;

    /** 计划配送时间 */
    private LocalDateTime planDeliveryTime;

    /** 实际提货时间 */
    private LocalDateTime actualPickupTime;

    /** 实际送达时间 */
    private LocalDateTime actualDeliveryTime;

    /** 发货地址 */
    private String fromAddress;

    /** 收货地址 */
    private String toAddress;

    /** 状态：1待提货 2配送中 3已送达 4拒收 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(fill = FieldFill.INSERT)
    private String createdBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;

    @TableLogic
    private Boolean deleted;

    // ==================== Getters and Setters ====================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getWaybillNo() { return waybillNo; }
    public void setWaybillNo(String waybillNo) { this.waybillNo = waybillNo; }

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

    public LocalDateTime getActualPickupTime() { return actualPickupTime; }
    public void setActualPickupTime(LocalDateTime actualPickupTime) { this.actualPickupTime = actualPickupTime; }

    public LocalDateTime getActualDeliveryTime() { return actualDeliveryTime; }
    public void setActualDeliveryTime(LocalDateTime actualDeliveryTime) { this.actualDeliveryTime = actualDeliveryTime; }

    public String getFromAddress() { return fromAddress; }
    public void setFromAddress(String fromAddress) { this.fromAddress = fromAddress; }

    public String getToAddress() { return toAddress; }
    public void setToAddress(String toAddress) { this.toAddress = toAddress; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public Boolean getDeleted() { return deleted; }
    public void setDeleted(Boolean deleted) { this.deleted = deleted; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TWaybill tWaybill = (TWaybill) o;
        return Objects.equals(id, tWaybill.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "TWaybill{id=" + id + ", waybillNo='" + waybillNo + "', status=" + status + "}";
    }
}
