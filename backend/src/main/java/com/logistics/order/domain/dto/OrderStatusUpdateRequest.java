package com.logistics.order.domain.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 订单状态更新请求
 */
public class OrderStatusUpdateRequest {

    @NotNull(message = "状态不能为空")
    private Integer status;

    private String remark;

    public OrderStatusUpdateRequest() {
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
