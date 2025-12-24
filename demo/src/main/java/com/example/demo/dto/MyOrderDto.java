package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MyOrderDto {
    public String id;                
    public long orderId;             
    public String createdAt;         
    public BigDecimal total;
    public String status;        
    public List<ItemDto> items = new ArrayList<>();

    public static class ItemDto {
        public String name;
        public int quantity;
        public BigDecimal price;   
    }
}
