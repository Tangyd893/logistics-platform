package com.logistics.common.enums;

/**
 * 通用状态枚举
 */
public enum CommonStatus {

    DISABLED(0, "禁用"),
    ENABLED(1, "启用");

    private final int code;
    private final String description;

    CommonStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static CommonStatus fromCode(int code) {
        for (CommonStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return DISABLED;
    }
}
