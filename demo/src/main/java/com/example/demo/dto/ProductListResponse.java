package com.example.demo.dto;

import java.util.List;

public class ProductListResponse {
	private List<ProductListItemDto> items;
    private PagingDto paging;

    public ProductListResponse() {}

    public ProductListResponse(List<ProductListItemDto> items, PagingDto paging) {
        this.items = items;
        this.paging = paging;
    }

    public List<ProductListItemDto> getItems() { return items; }
    public void setItems(List<ProductListItemDto> items) { this.items = items; }

    public PagingDto getPaging() { return paging; }
    public void setPaging(PagingDto paging) { this.paging = paging; }

}
