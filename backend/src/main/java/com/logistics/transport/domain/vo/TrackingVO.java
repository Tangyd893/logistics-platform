package com.logistics.transport.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 配送轨迹视图
 */
public class TrackingVO {

    private Long id;
    private Long waybillId;
    private Integer status;
    private String statusName;
    private String location;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String description;
    private LocalDateTime operateTime;

    public TrackingVO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getWaybillId() { return waybillId; }
    public void setWaybillId(Long waybillId) { this.waybillId = waybillId; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getStatusName() { return statusName; }
    public void setStatusName(String statusName) { this.statusName = statusName; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getOperateTime() { return operateTime; }
    public void setOperateTime(LocalDateTime operateTime) { this.operateTime = operateTime; }
}
