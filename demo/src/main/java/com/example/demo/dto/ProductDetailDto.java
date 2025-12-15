package com.example.demo.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProductDetailDto {
	  public long id;
	  public String name;
	  public String slug;
	  public String sku;
	  public BigDecimal price;
	  public BigDecimal originalPrice;
	  public String mainImage;
	  public String description;

	  public double rating;
	  public int reviewCount;

	  public List<String> gallery = new ArrayList<>();

	  // ✅ list variants thật sự
	  public List<VariantItemDto> variantList = new ArrayList<>();

	  public VariantUiDto variants; 
	  public Map<String, String> specs;
	  public List<RelatedProductDto> relatedProducts = new ArrayList<>();

	  public static class VariantItemDto {
	    public Long variantId;
	    public String material;
	    public String color;
	    public String size;
	    public BigDecimal price;
	  }

	  public static class VariantUiDto {
	    public List<ColorDto> colors;
	    public List<SizeDto> sizes;
	  }

	  public static class ColorDto { public String name; public String hex; }
	  public static class SizeDto { public String label; public BigDecimal price; }

	  public static class RelatedProductDto {
	    public long id;
	    public String name;
	    public String slug;
	    public BigDecimal price;
	    public String image;
	  }
	}