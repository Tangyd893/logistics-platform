package com.logistics.system.domain.dto;

import jakarta.validation.constraints.NotBlank;

public class DeptCreateRequest {
    private Long parentId = 0L;
    @NotBlank(message = "部门名称不能为空")
    private String name;
    private Integer sortOrder = 0;

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
