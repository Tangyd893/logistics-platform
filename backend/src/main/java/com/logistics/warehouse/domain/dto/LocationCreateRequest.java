package com.logistics.warehouse.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class LocationCreateRequest {
    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    private Long zoneId;

    @NotBlank(message = "库位编码不能为空")
    private String code;

    private String type = "SHELF";
    private Integer shelfLayer;
    private BigDecimal capacity;
    private Integer status = 1;

    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public Long getZoneId() { return zoneId; }
    public void setZoneId(Long zoneId) { this.zoneId = zoneId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getShelfLayer() { return shelfLayer; }
    public void setShelfLayer(Integer shelfLayer) { this.shelfLayer = shelfLayer; }
    public BigDecimal getCapacity() { return capacity; }
    public void setCapacity(BigDecimal capacity) { this.capacity = capacity; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
