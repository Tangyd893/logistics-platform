package com.logistics.common.enums;

/**
 * 订单状态枚举
 */
public enum OrderStatus {

    PENDING(10, "待确认"),
    CONFIRMED(20, "已确认"),
    IN_WAREHOUSE(30, "已入库"),
    SHIPPED(40, "已发货"),
    IN_TRANSIT(50, "运输中"),
    DELIVERED(60, "已送达"),
    COMPLETED(70, "已完成"),
    CANCELLED(80, "已取消");

    private final int code;
    private final String description;

    OrderStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static OrderStatus fromCode(int code) {
        for (OrderStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown order status code: " + code);
    }
}
