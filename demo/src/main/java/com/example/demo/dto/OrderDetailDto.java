package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDetailDto {
    public long orderId;

    public String status;         
    public String paymentStatus; 
    public String paymentMethod;  

    public BigDecimal subtotal;
    public BigDecimal discount;
    public BigDecimal shippingFee;
    public BigDecimal total;

    public String promotionCode;
    public LocalDateTime createdAt;

    public ShippingInfoDto shippingInfo;
    public List<ItemDto> items;

    public static class ShippingInfoDto {
        public String fullName;
        public String phone;
        public String city;      
        public String district;
        public String ward;
        public String address;
    }

    public static class ItemDto {
        public String name;
        public String variantInfo;
        public int quantity;
        public BigDecimal unitPrice;
        public BigDecimal lineTotal;
    }
}
