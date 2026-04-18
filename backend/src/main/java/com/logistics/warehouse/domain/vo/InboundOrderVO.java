package com.logistics.warehouse.domain.vo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 入库单视图对象
 */
public class InboundOrderVO {

    private Long id;
    private String orderNo;
    private Long warehouseId;
    private String warehouseName;
    private String supplierName;
    private LocalDateTime expectedArrivalTime;
    private LocalDateTime actualArrivalTime;
    private String inboundType;
    private String inboundTypeName;
    private Integer status;
    private String statusName;
    private String remark;
    private String operator;
    private List<InboundItemVO> items;
    private LocalDateTime createdAt;

    public InboundOrderVO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }

    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public LocalDateTime getExpectedArrivalTime() { return expectedArrivalTime; }
    public void setExpectedArrivalTime(LocalDateTime expectedArrivalTime) { this.expectedArrivalTime = expectedArrivalTime; }

    public LocalDateTime getActualArrivalTime() { return actualArrivalTime; }
    public void setActualArrivalTime(LocalDateTime actualArrivalTime) { this.actualArrivalTime = actualArrivalTime; }

    public String getInboundType() { return inboundType; }
    public void setInboundType(String inboundType) { this.inboundType = inboundType; }

    public String getInboundTypeName() { return inboundTypeName; }
    public void setInboundTypeName(String inboundTypeName) { this.inboundTypeName = inboundTypeName; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getStatusName() { return statusName; }
    public void setStatusName(String statusName) { this.statusName = statusName; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }

    public List<InboundItemVO> getItems() { return items; }
    public void setItems(List<InboundItemVO> items) { this.items = items; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /**
     * 入库明细视图
     */
    public static class InboundItemVO {
        private Long id;
        private String sku;
        private String goodsName;
        private java.math.BigDecimal expectedQty;
        private java.math.BigDecimal actualQty;
        private String unit;
        private java.math.BigDecimal unitPrice;
        private String batchNo;
        private LocalDateTime productionDate;
        private LocalDateTime expiryDate;
        private Long locationId;
        private String locationCode;
        private Integer status;
        private String statusName;

        public InboundItemVO() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }

        public String getGoodsName() { return goodsName; }
        public void setGoodsName(String goodsName) { this.goodsName = goodsName; }

        public java.math.BigDecimal getExpectedQty() { return expectedQty; }
        public void setExpectedQty(java.math.BigDecimal expectedQty) { this.expectedQty = expectedQty; }

        public java.math.BigDecimal getActualQty() { return actualQty; }
        public void setActualQty(java.math.BigDecimal actualQty) { this.actualQty = actualQty; }

        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }

        public java.math.BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(java.math.BigDecimal unitPrice) { this.unitPrice = unitPrice; }

        public String getBatchNo() { return batchNo; }
        public void setBatchNo(String batchNo) { this.batchNo = batchNo; }

        public LocalDateTime getProductionDate() { return productionDate; }
        public void setProductionDate(LocalDateTime productionDate) { this.productionDate = productionDate; }

        public LocalDateTime getExpiryDate() { return expiryDate; }
        public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }

        public Long getLocationId() { return locationId; }
        public void setLocationId(Long locationId) { this.locationId = locationId; }

        public String getLocationCode() { return locationCode; }
        public void setLocationCode(String locationCode) { this.locationCode = locationCode; }

        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }

        public String getStatusName() { return statusName; }
        public void setStatusName(String statusName) { this.statusName = statusName; }
    }
}
