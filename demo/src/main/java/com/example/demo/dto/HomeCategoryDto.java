package com.example.demo.dto;

public class HomeCategoryDto {
	private long id;
    private String title;
    private String slug;
    private String image;
    
    public HomeCategoryDto() {}

    public HomeCategoryDto(long id, String title, String slug, String image) {
        this.id = id;
        this.title = title;
        this.slug = slug;
        this.image = image;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

}
