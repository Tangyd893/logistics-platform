package com.logistics.warehouse.domain.vo;

import java.math.BigDecimal;

/**
 * 仓库视图对象
 */
public class WarehouseVO {

    private Long id;
    private String code;
    private String name;
    private String address;
    private String manager;
    private String phone;
    private BigDecimal totalCapacity;
    private BigDecimal usedCapacity;
    private BigDecimal availableCapacity;
    private Integer status;
    private String statusName;
    private String remark;

    public WarehouseVO() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getManager() { return manager; }
    public void setManager(String manager) { this.manager = manager; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public BigDecimal getTotalCapacity() { return totalCapacity; }
    public void setTotalCapacity(BigDecimal totalCapacity) { this.totalCapacity = totalCapacity; }

    public BigDecimal getUsedCapacity() { return usedCapacity; }
    public void setUsedCapacity(BigDecimal usedCapacity) { this.usedCapacity = usedCapacity; }

    public BigDecimal getAvailableCapacity() { return availableCapacity; }
    public void setAvailableCapacity(BigDecimal availableCapacity) { this.availableCapacity = availableCapacity; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getStatusName() { return statusName; }
    public void setStatusName(String statusName) { this.statusName = statusName; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
