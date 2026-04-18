package com.logistics.warehouse.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 仓库创建/更新请求
 */
public class WarehouseCreateRequest {

    private Long id;

    @NotBlank(message = "仓库编码不能为空")
    private String code;

    @NotBlank(message = "仓库名称不能为空")
    private String name;

    private String address;
    private String manager;
    private String phone;

    @NotNull(message = "总容量不能为空")
    private BigDecimal totalCapacity;

    private Integer status = 1;
    private String remark;

    public WarehouseCreateRequest() {
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

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
