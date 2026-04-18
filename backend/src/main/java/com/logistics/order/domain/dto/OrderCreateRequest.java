package com.logistics.order.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * 订单创建/更新请求
 */
public class OrderCreateRequest {

    private Long id;

    private Long customerId;

    @NotBlank(message = "发货人不能为空")
    private String senderName;

    @NotBlank(message = "发货人电话不能为空")
    private String senderPhone;

    @NotBlank(message = "发货人地址不能为空")
    private String senderAddress;

    @NotBlank(message = "收货人不能为空")
    private String receiverName;

    @NotBlank(message = "收货人电话不能为空")
    private String receiverPhone;

    @NotBlank(message = "收货人地址不能为空")
    private String receiverAddress;

    private BigDecimal totalAmount;

    private BigDecimal weightKg;

    private BigDecimal volumeCbm;

    private String remark;

    @NotNull(message = "订单明细不能为空")
    private List<ItemRequest> items;

    public OrderCreateRequest() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getSenderPhone() { return senderPhone; }
    public void setSenderPhone(String senderPhone) { this.senderPhone = senderPhone; }

    public String getSenderAddress() { return senderAddress; }
    public void setSenderAddress(String senderAddress) { this.senderAddress = senderAddress; }

    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }

    public String getReceiverPhone() { return receiverPhone; }
    public void setReceiverPhone(String receiverPhone) { this.receiverPhone = receiverPhone; }

    public String getReceiverAddress() { return receiverAddress; }
    public void setReceiverAddress(String receiverAddress) { this.receiverAddress = receiverAddress; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getWeightKg() { return weightKg; }
    public void setWeightKg(BigDecimal weightKg) { this.weightKg = weightKg; }

    public BigDecimal getVolumeCbm() { return volumeCbm; }
    public void setVolumeCbm(BigDecimal volumeCbm) { this.volumeCbm = volumeCbm; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public List<ItemRequest> getItems() { return items; }
    public void setItems(List<ItemRequest> items) { this.items = items; }

    /**
     * 订单明细项
     */
    public static class ItemRequest {
        @NotBlank(message = "货品名称不能为空")
        private String skuName;
        private String skuCode;

        @NotNull(message = "数量不能为空")
        private Integer quantity;

        private BigDecimal weightKg;
        private BigDecimal volumeCbm;
        private BigDecimal unitPrice;

        public ItemRequest() {}

        public String getSkuName() { return skuName; }
        public void setSkuName(String skuName) { this.skuName = skuName; }

        public String getSkuCode() { return skuCode; }
        public void setSkuCode(String skuCode) { this.skuCode = skuCode; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }

        public BigDecimal getWeightKg() { return weightKg; }
        public void setWeightKg(BigDecimal weightKg) { this.weightKg = weightKg; }

        public BigDecimal getVolumeCbm() { return volumeCbm; }
        public void setVolumeCbm(BigDecimal volumeCbm) { this.volumeCbm = volumeCbm; }

        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    }
}
