package com.example.demo.dto;

public class PagingDto {
	private int page;
    private int size;
    private long total;
    private int totalPages;

    public PagingDto() {}

    public PagingDto(int page, int size, long total) {
        this.page = page;
        this.size = size;
        this.total = total;
        this.totalPages = (int) Math.ceil(total * 1.0 / size);
    }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

}
