package com.example.demo.dto.admin;

import java.math.BigDecimal;

public class AdminOrderItemDto {
  public long orderItemId;
  public long productId;
  public Long variantId;

  public String productName;
  public String variantInfo;

  public int quantity;
  public BigDecimal unitPrice;
  public BigDecimal totalPrice;
}
