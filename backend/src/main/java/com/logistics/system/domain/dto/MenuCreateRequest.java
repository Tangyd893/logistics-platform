package com.logistics.system.domain.dto;

import jakarta.validation.constraints.NotBlank;

public class MenuCreateRequest {
    private Long parentId = 0L;
    @NotBlank(message = "菜单名称不能为空")
    private String name;
    private String path;
    private String component;
    private String icon;
    private Integer sortOrder = 0;
    private Integer type = 1; // 1=菜单 2=按钮
    private String perms;
    private Integer status = 1;

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getComponent() { return component; }
    public void setComponent(String component) { this.component = component; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }
    public String getPerms() { return perms; }
    public void setPerms(String perms) { this.perms = perms; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
