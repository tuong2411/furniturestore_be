package com.example.demo.repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.demo.dto.ProductListItemDto;

@Repository
public class ProductRepository {

	private final JdbcTemplate jdbcTemplate;

    public ProductRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long countProducts(String q, String categorySlug, List<String> materials, BigDecimal minPrice, BigDecimal maxPrice) {
        StringBuilder sql = new StringBuilder();
        List<Object> args = new ArrayList<>();

        sql.append("""
            SELECT COUNT(DISTINCT p.product_id)
            FROM products p
            JOIN categories c ON c.category_id = p.category_id
        """);

        // Filter material qua variants
        if (materials != null && !materials.isEmpty()) {
            sql.append("""
                JOIN product_variants pv ON pv.product_id = p.product_id AND pv.is_active = 1
            """);
        }

        sql.append(" WHERE p.is_active = 1 ");

        if (q != null && !q.trim().isEmpty()) {
            sql.append(" AND (p.name LIKE ? OR p.short_desc LIKE ? OR p.description LIKE ?) ");
            String kw = "%" + q.trim() + "%";
            args.add(kw); args.add(kw); args.add(kw);
        }

        if (categorySlug != null && !categorySlug.trim().isEmpty()) {
            sql.append("""
                AND (
                    c.slug = ?
                    OR c.parent_id = (SELECT category_id FROM categories WHERE slug = ? LIMIT 1)
                )
            """);
            args.add(categorySlug.trim());
            args.add(categorySlug.trim());
        }

        if (minPrice != null) {
            sql.append(" AND p.base_price >= ? ");
            args.add(minPrice);
        }
        if (maxPrice != null) {
            sql.append(" AND p.base_price <= ? ");
            args.add(maxPrice);
        }

        if (materials != null && !materials.isEmpty()) {
            sql.append(" AND pv.material IN (");
            sql.append("?, ".repeat(materials.size()));
            sql.setLength(sql.length() - 2);
            sql.append(") ");
            args.addAll(materials);
        }

        return jdbcTemplate.queryForObject(sql.toString(), args.toArray(), Long.class);
    }
    
    public List<ProductListItemDto> findProducts(
            String q, String categorySlug, List<String> materials,
            BigDecimal minPrice, BigDecimal maxPrice,
            int page, int size, String sort
    ) {
        StringBuilder sql = new StringBuilder();
        List<Object> args = new ArrayList<>();

        sql.append("""
            SELECT DISTINCT
                p.product_id, p.name, p.slug, p.base_price, p.main_image, p.created_at
            FROM products p
            JOIN categories c ON c.category_id = p.category_id
        """);

        if (materials != null && !materials.isEmpty()) {
            sql.append("""
                JOIN product_variants pv ON pv.product_id = p.product_id AND pv.is_active = 1
            """);
        }

        sql.append(" WHERE p.is_active = 1 ");

        if (q != null && !q.trim().isEmpty()) {
            sql.append(" AND (p.name LIKE ? OR p.short_desc LIKE ? OR p.description LIKE ?) ");
            String kw = "%" + q.trim() + "%";
            args.add(kw); args.add(kw); args.add(kw);
        }

        if (categorySlug != null && !categorySlug.trim().isEmpty()) {
            sql.append("""
                AND (
                    c.slug = ?
                    OR c.parent_id = (SELECT category_id FROM categories WHERE slug = ? LIMIT 1)
                )
            """);
            args.add(categorySlug.trim());
            args.add(categorySlug.trim());
        }

        if (minPrice != null) {
            sql.append(" AND p.base_price >= ? ");
            args.add(minPrice);
        }
        if (maxPrice != null) {
            sql.append(" AND p.base_price <= ? ");
            args.add(maxPrice);
        }

        if (materials != null && !materials.isEmpty()) {
            sql.append(" AND pv.material IN (");
            sql.append("?, ".repeat(materials.size()));
            sql.setLength(sql.length() - 2);
            sql.append(") ");
            args.addAll(materials);
        }

        sql.append(" ORDER BY ");
        sql.append(resolveSort(sort));

        sql.append(" LIMIT ? OFFSET ? ");
        args.add(size);
        args.add((page - 1) * size);

        return jdbcTemplate.query(sql.toString(), (ResultSet rs, int rowNum) ->
                new ProductListItemDto(
                        rs.getLong("product_id"),
                        rs.getString("name"),
                        rs.getString("slug"),
                        rs.getBigDecimal("base_price"),
                        rs.getString("main_image")
                ), args.toArray());
    }
    
    private String resolveSort(String sort) {
        if (sort == null) return "p.created_at DESC, p.product_id DESC";
        return switch (sort) {
            case "price_asc" -> "p.base_price ASC";
            case "price_desc" -> "p.base_price DESC";
            case "newest" -> "p.created_at DESC, p.product_id DESC";
            default -> "p.created_at DESC, p.product_id DESC";
        };
    }
}
