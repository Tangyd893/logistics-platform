package com.logistics.warehouse.domain.vo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 出库单视图对象
 */
public class OutboundOrderVO {

    private Long id;
    private String orderNo;
    private Long warehouseId;
    private String warehouseName;
    private String customerName;
    private String customerAddress;
    private String customerPhone;
    private String outboundType;
    private String outboundTypeName;
    private Integer status;
    private String statusName;
    private String remark;
    private String operator;
    private List<OutboundItemVO> items;
    private LocalDateTime createdAt;

    public OutboundOrderVO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }

    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerAddress() { return customerAddress; }
    public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getOutboundType() { return outboundType; }
    public void setOutboundType(String outboundType) { this.outboundType = outboundType; }

    public String getOutboundTypeName() { return outboundTypeName; }
    public void setOutboundTypeName(String outboundTypeName) { this.outboundTypeName = outboundTypeName; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getStatusName() { return statusName; }
    public void setStatusName(String statusName) { this.statusName = statusName; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }

    public List<OutboundItemVO> getItems() { return items; }
    public void setItems(List<OutboundItemVO> items) { this.items = items; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /**
     * 出库明细视图
     */
    public static class OutboundItemVO {
        private Long id;
        private String sku;
        private String goodsName;
        private java.math.BigDecimal orderQty;
        private java.math.BigDecimal pickedQty;
        private String unit;
        private String batchNo;
        private Long locationId;
        private String locationCode;
        private Long inventoryId;
        private Integer status;
        private String statusName;

        public OutboundItemVO() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }

        public String getGoodsName() { return goodsName; }
        public void setGoodsName(String goodsName) { this.goodsName = goodsName; }

        public java.math.BigDecimal getOrderQty() { return orderQty; }
        public void setOrderQty(java.math.BigDecimal orderQty) { this.orderQty = orderQty; }

        public java.math.BigDecimal getPickedQty() { return pickedQty; }
        public void setPickedQty(java.math.BigDecimal pickedQty) { this.pickedQty = pickedQty; }

        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }

        public String getBatchNo() { return batchNo; }
        public void setBatchNo(String batchNo) { this.batchNo = batchNo; }

        public Long getLocationId() { return locationId; }
        public void setLocationId(Long locationId) { this.locationId = locationId; }

        public String getLocationCode() { return locationCode; }
        public void setLocationCode(String locationCode) { this.locationCode = locationCode; }

        public Long getInventoryId() { return inventoryId; }
        public void setInventoryId(Long inventoryId) { this.inventoryId = inventoryId; }

        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }

        public String getStatusName() { return statusName; }
        public void setStatusName(String statusName) { this.statusName = statusName; }
    }
}
