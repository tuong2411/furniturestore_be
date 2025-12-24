package com.example.demo.dto.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AdminOrderRowDto {
  public long orderId;
  public LocalDateTime createdAt;

  public long userId;
  public String customerName;
  public String customerEmail;

  public BigDecimal totalAmount;
  public String status;
  public String paymentStatus;
  public String paymentMethod;
}
