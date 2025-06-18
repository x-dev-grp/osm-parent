package com.xdev.xdevbase.apiDTOs;

import com.xdev.xdevbase.dtos.BaseDto;
import com.xdev.xdevbase.entities.BaseEntity;

import java.util.List;

public class SearchResponse<T extends BaseEntity, D extends BaseDto<T>> {
    private long total;
    private List<D> data;
    private int totalPages;
    private int page;

    public SearchResponse(long total, List<D> data, int totalPages, int page) {
        this.total = total;
        this.data = data;
        this.totalPages = totalPages;
        this.page = page;
    }

    // Getters and setters
    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<D> getData() {
        return data;
    }

    public void setData(List<D> data) {
        this.data = data;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }
}