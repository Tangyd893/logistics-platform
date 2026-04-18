package com.logistics.warehouse.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 出库单创建请求
 */
public class OutboundOrderCreateRequest {

    private Long id;

    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    @NotBlank(message = "客户名称不能为空")
    private String customerName;

    @NotBlank(message = "客户地址不能为空")
    private String customerAddress;

    private String customerPhone;

    @NotBlank(message = "出库类型不能为空")
    private String outboundType;

    private String remark;
    private List<ItemRequest> items;

    public OutboundOrderCreateRequest() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerAddress() { return customerAddress; }
    public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getOutboundType() { return outboundType; }
    public void setOutboundType(String outboundType) { this.outboundType = outboundType; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public List<ItemRequest> getItems() { return items; }
    public void setItems(List<ItemRequest> items) { this.items = items; }

    /**
     * 出库明细项
     */
    public static class ItemRequest {
        @NotBlank(message = "商品SKU不能为空")
        private String sku;
        @NotBlank(message = "商品名称不能为空")
        private String goodsName;
        @NotNull(message = "订单数量不能为空")
        private java.math.BigDecimal orderQty;
        private String unit;
        private String batchNo;
        private Long locationId;

        public ItemRequest() {}

        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }

        public String getGoodsName() { return goodsName; }
        public void setGoodsName(String goodsName) { this.goodsName = goodsName; }

        public java.math.BigDecimal getOrderQty() { return orderQty; }
        public void setOrderQty(java.math.BigDecimal orderQty) { this.orderQty = orderQty; }

        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }

        public String getBatchNo() { return batchNo; }
        public void setBatchNo(String batchNo) { this.batchNo = batchNo; }

        public Long getLocationId() { return locationId; }
        public void setLocationId(Long locationId) { this.locationId = locationId; }
    }
}
