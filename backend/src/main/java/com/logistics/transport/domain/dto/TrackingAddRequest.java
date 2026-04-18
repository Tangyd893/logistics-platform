package com.logistics.transport.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 轨迹录入请求
 */
public class TrackingAddRequest {

    @NotNull(message = "运单ID不能为空")
    private Long waybillId;

    private String location;
    private BigDecimal latitude;
    private BigDecimal longitude;

    @NotBlank(message = "描述不能为空")
    private String description;

    public TrackingAddRequest() {}

    public Long getWaybillId() { return waybillId; }
    public void setWaybillId(Long waybillId) { this.waybillId = waybillId; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
