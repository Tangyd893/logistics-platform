package com.logistics.common.dto;

import java.util.List;

/**
 * 分页响应
 */
public class PageDTO<T> {

    private List<T> records;
    private long total;
    private long page;
    private long size;
    private long pages;

    public PageDTO() {
    }

    public PageDTO(List<T> records, long total, long page, long size, long pages) {
        this.records = records;
        this.total = total;
        this.page = page;
        this.size = size;
        this.pages = pages;
    }

    public static <T> PageDTO<T> of(List<T> records, long total, long page, long size) {
        long pages = size > 0 ? (total + size - 1) / size : 0;
        return new PageDTO<>(records, total, page, size, pages);
    }

    // ==================== Getters and Setters ====================

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getPage() {
        return page;
    }

    public void setPage(long page) {
        this.page = page;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getPages() {
        return pages;
    }

    public void setPages(long pages) {
        this.pages = pages;
    }
}
