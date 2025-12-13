package com.example.demo.dto;

import java.util.ArrayList;
import java.util.List;

public class CategoryTreeDto {

	private long id;
    private String name;
    private String slug;
    private String description;
    private Integer displayOrder;
    private List<CategoryTreeDto> children = new ArrayList<>();
    
    public CategoryTreeDto() {}

    public CategoryTreeDto(long id, String name, String slug, String description, Integer displayOrder) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.displayOrder = displayOrder;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public List<CategoryTreeDto> getChildren() { return children; }
    public void setChildren(List<CategoryTreeDto> children) { this.children = children; }

    public void addChild(CategoryTreeDto child) { this.children.add(child); }
}
