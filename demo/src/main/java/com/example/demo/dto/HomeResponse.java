package com.example.demo.dto;

import java.util.List;

public class HomeResponse {
	private List<HomeCategoryDto> categories;
    private List<HomeProductDto> featuredProducts;
    
    public HomeResponse() {}

    public HomeResponse(List<HomeCategoryDto> categories, List<HomeProductDto> featuredProducts) {
        this.categories = categories;
        this.featuredProducts = featuredProducts;
    }

    public List<HomeCategoryDto> getCategories() { return categories; }
    public void setCategories(List<HomeCategoryDto> categories) { this.categories = categories; }

    public List<HomeProductDto> getFeaturedProducts() { return featuredProducts; }
    public void setFeaturedProducts(List<HomeProductDto> featuredProducts) { this.featuredProducts = featuredProducts; }

}
