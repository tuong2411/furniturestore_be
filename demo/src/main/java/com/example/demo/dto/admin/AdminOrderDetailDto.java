package com.example.demo.dto.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class AdminOrderDetailDto {
  public long orderId;
  public LocalDateTime createdAt;
  public LocalDateTime updatedAt;

  public long userId;
  public String customerName;
  public String customerEmail;
  public String customerPhone;

  public String status;
  public String paymentStatus;
  public String paymentMethod;

  public BigDecimal subtotalAmount;
  public BigDecimal discountAmount;
  public BigDecimal shippingFee;
  public BigDecimal totalAmount;

  public String promotionCode;
  public String orderNote;

  // address
  public Long addressId;
  public String addressFullName;
  public String addressPhone;
  public String province;
  public String district;
  public String ward;
  public String street;

  public List<AdminOrderItemDto> items;
}
