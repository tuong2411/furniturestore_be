package com.example.demo.dto.admin;

import java.math.BigDecimal;

public class VariantUpsertRequest {
	 public Long productId;      
	  public String sku;
	  public String color;
	  public String size;
	  public String material;
	  public String extraDesc;
	  public BigDecimal price;
	  public Integer stock;
	  public Boolean isActive;

}
