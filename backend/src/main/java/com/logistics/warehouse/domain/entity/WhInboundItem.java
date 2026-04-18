package com.logistics.warehouse.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 入库明细
 */
@TableName("wh_inbound_item")
public class WhInboundItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 入库单ID */
    private Long inboundId;

    /** 商品SKU */
    private String sku;

    /** 商品名称 */
    private String goodsName;

    /** 预期数量 */
    private BigDecimal expectedQty;

    /** 实际数量 */
    private BigDecimal actualQty;

    /** 计量单位 */
    private String unit;

    /** 单价 */
    private BigDecimal unitPrice;

    /** 批次号 */
    private String batchNo;

    /** 生产日期 */
    private LocalDateTime productionDate;

    /** 过期日期 */
    private LocalDateTime expiryDate;

    /** 分配库位ID */
    private Long locationId;

    /** 状态：10待入库 20部分入库 30已完成 */
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

    public Long getInboundId() { return inboundId; }
    public void setInboundId(Long inboundId) { this.inboundId = inboundId; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getGoodsName() { return goodsName; }
    public void setGoodsName(String goodsName) { this.goodsName = goodsName; }

    public BigDecimal getExpectedQty() { return expectedQty; }
    public void setExpectedQty(BigDecimal expectedQty) { this.expectedQty = expectedQty; }

    public BigDecimal getActualQty() { return actualQty; }
    public void setActualQty(BigDecimal actualQty) { this.actualQty = actualQty; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }

    public LocalDateTime getProductionDate() { return productionDate; }
    public void setProductionDate(LocalDateTime productionDate) { this.productionDate = productionDate; }

    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }

    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }

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
        WhInboundItem that = (WhInboundItem) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "WhInboundItem{id=" + id + ", inboundId=" + inboundId + ", sku='" + sku + "'}";
    }
}
