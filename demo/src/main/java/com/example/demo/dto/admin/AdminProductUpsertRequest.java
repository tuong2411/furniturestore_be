package com.example.demo.dto.admin;

import java.math.BigDecimal;

public class AdminProductUpsertRequest {
	public Long categoryId;
	  public String name;
	  public String slug;
	  public String sku;
	  public String shortDesc;
	  public String description;
	  public BigDecimal basePrice;
	  public Integer baseStock;
	  public String mainImage;
	  public Boolean isActive;

}
