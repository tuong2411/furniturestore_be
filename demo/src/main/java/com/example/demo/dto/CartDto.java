package com.example.demo.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CartDto {
	public long cartId;
    public List<CartItemDto> items = new ArrayList<>();

    public BigDecimal subtotal = BigDecimal.ZERO;
    public BigDecimal shippingFee = BigDecimal.ZERO;
    public BigDecimal total = BigDecimal.ZERO;

    public static class CartItemDto {
        public long cartItemId;
        public long productId;
        public String name;
        public String slug;
        public String image;

        public Long variantId; 
        public String material; 
        public String color;    
        public String size;     

        public BigDecimal unitPrice = BigDecimal.ZERO;
        public int quantity;
        public BigDecimal lineTotal = BigDecimal.ZERO;
    }

}
