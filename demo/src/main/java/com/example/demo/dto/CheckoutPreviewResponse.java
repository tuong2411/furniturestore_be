package com.example.demo.dto;

import java.math.BigDecimal;

public class CheckoutPreviewResponse {
	public BigDecimal subtotal;
    public BigDecimal discount;
    public BigDecimal shippingFee;
    public BigDecimal total;

    public String promotionCode;

}
