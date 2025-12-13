package com.example.demo.dto;

import java.math.BigDecimal;

public class HomeProductDto {
	private long id;
    private String name;
    private String slug;
    private BigDecimal price;
    private String image;

    public HomeProductDto() {}

    public HomeProductDto(long id, String name, String slug, BigDecimal price, String image) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.price = price;
        this.image = image;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

}
