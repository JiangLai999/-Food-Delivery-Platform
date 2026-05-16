package com.fooddelivery.user.model;

import java.util.List;

/**
 * 分页结果模型
 */
public class PageResult<T> {
    private List<T> records;
    private Long total;
    private Long size;
    private Long current;
    private Long pages;

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Long getCurrent() {
        return current;
    }

    public void setCurrent(Long current) {
        this.current = current;
    }

    public Long getPages() {
        return pages;
    }

    public void setPages(Long pages) {
        this.pages = pages;
    }

    /**
     * 是否有下一页
     */
    public boolean hasNext() {
        return current != null && pages != null && current < pages;
    }

    /**
     * 是否有上一页
     */
    public boolean hasPrevious() {
        return current != null && current > 1;
    }
}
