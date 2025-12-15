package com.example.demo.dto;

public class CheckoutRequest {
	public java.util.List<Long> cartItemIds;
	public ShippingInfo shippingInfo;
	public String paymentMethod; 
	public String note; 
	public String promotionCode;

	public static class ShippingInfo {
		public String fullName;
		public String phone;
		public String email; 
		public String address; 
		public String city; 
		public String district;
		public String ward;
	}

}
