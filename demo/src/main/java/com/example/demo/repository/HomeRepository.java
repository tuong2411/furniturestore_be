package com.example.demo.repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.demo.dto.HomeCategoryDto;
import com.example.demo.dto.HomeProductDto;

@Repository
public class HomeRepository {
	private final JdbcTemplate jdbcTemplate;

    public HomeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    public List<HomeCategoryDto> findTopParentCategories(int limit) {
        String sql = """
            SELECT category_id, name, slug
            FROM categories
            WHERE parent_id IS NULL AND is_active = 1
            ORDER BY display_order ASC, category_id ASC
            LIMIT ?
        """;

        return jdbcTemplate.query(sql, (ResultSet rs, int rowNum) -> {
            long id = rs.getLong("category_id");
            String title = rs.getString("name");
            String slug = rs.getString("slug");
            return new HomeCategoryDto(id, title, slug, null);
        }, limit);
    }

    public List<HomeProductDto> findFeaturedProducts(int limit) {
        
        String sql = """
            SELECT product_id, name, slug, base_price, main_image
            FROM products
            WHERE is_active = 1
            ORDER BY created_at DESC, product_id DESC
            LIMIT ?
        """;

        return jdbcTemplate.query(sql, (ResultSet rs, int rowNum) -> {
            long id = rs.getLong("product_id");
            String name = rs.getString("name");
            String slug = rs.getString("slug");
            BigDecimal price = rs.getBigDecimal("base_price");
            String image = rs.getString("main_image");
            return new HomeProductDto(id, name, slug, price, image);
        }, limit);
    }
}
