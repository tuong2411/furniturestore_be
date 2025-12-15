package com.example.demo.dto;

public class CheckoutResponse {

	public long orderId;
	public java.math.BigDecimal subtotal;
	public java.math.BigDecimal discount;
	public java.math.BigDecimal shippingFee;
	public java.math.BigDecimal total;
	public String status;
	public String paymentStatus;
	public String paymentMethod;
}
