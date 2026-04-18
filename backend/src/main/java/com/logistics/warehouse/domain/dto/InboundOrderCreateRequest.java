package com.logistics.warehouse.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 入库单创建请求
 */
public class InboundOrderCreateRequest {

    private Long id;

    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    @NotBlank(message = "供应商名称不能为空")
    private String supplierName;

    private LocalDateTime expectedArrivalTime;

    @NotBlank(message = "入库类型不能为空")
    private String inboundType;

    private String remark;
    private List<ItemRequest> items;

    public InboundOrderCreateRequest() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public LocalDateTime getExpectedArrivalTime() { return expectedArrivalTime; }
    public void setExpectedArrivalTime(LocalDateTime expectedArrivalTime) { this.expectedArrivalTime = expectedArrivalTime; }

    public String getInboundType() { return inboundType; }
    public void setInboundType(String inboundType) { this.inboundType = inboundType; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public List<ItemRequest> getItems() { return items; }
    public void setItems(List<ItemRequest> items) { this.items = items; }

    /**
     * 入库明细项
     */
    public static class ItemRequest {
        @NotBlank(message = "商品SKU不能为空")
        private String sku;
        @NotBlank(message = "商品名称不能为空")
        private String goodsName;
        @NotNull(message = "预期数量不能为空")
        private java.math.BigDecimal expectedQty;
        private String unit;
        private java.math.BigDecimal unitPrice;
        private String batchNo;
        private LocalDateTime productionDate;
        private LocalDateTime expiryDate;
        private Long locationId;

        public ItemRequest() {}

        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }

        public String getGoodsName() { return goodsName; }
        public void setGoodsName(String goodsName) { this.goodsName = goodsName; }

        public java.math.BigDecimal getExpectedQty() { return expectedQty; }
        public void setExpectedQty(java.math.BigDecimal expectedQty) { this.expectedQty = expectedQty; }

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
    }
}
