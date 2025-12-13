package com.example.demo.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class ProductDetailDto {
	public long id;
    public String name;
    public String slug;
    public String sku;

    public BigDecimal price;
    public BigDecimal originalPrice;

    public Double rating;
    public Integer reviewCount;

    public String mainImage;
    public List<String> gallery;

    public String description;
    public Map<String, String> specs;

    public VariantDto variants;
    public List<RelatedProductDto> relatedProducts;

    public static class VariantDto {
        public List<ColorDto> colors;
        public List<SizeDto> sizes;
    }

    public static class ColorDto {
        public String name;
        public String hex;
    }

    public static class SizeDto {
        public String label;
        public BigDecimal price;
    }

    public static class RelatedProductDto {
        public long id;
        public String name;
        public BigDecimal price;
        public String image;
        public String slug;
    }

}
