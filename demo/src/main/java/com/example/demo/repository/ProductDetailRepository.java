package com.example.demo.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ProductDetailRepository {

    private final JdbcTemplate jdbc;

    public ProductDetailRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<Map<String, Object>> findProductBySlug(String slug) {
        String sql = """
            SELECT product_id, category_id, name, slug, sku, base_price,
                   description, main_image, created_at
            FROM products
            WHERE slug = ? AND is_active = 1
            LIMIT 1
        """;

        List<Map<String, Object>> rows = jdbc.queryForList(sql, slug);
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(rows.get(0));
    }

    public List<String> findGallery(long productId) {
        return jdbc.queryForList(
            "SELECT image_url FROM product_images WHERE product_id = ? ORDER BY display_order",
            String.class,
            productId
        );
    }

    public List<Map<String, Object>> findVariants(long productId) {
    	  return jdbc.queryForList("""
    	      SELECT variant_id, material, color, size, price
    	      FROM product_variants
    	      WHERE product_id = ? AND is_active = 1
    	      ORDER BY variant_id ASC
    	  """, productId);
    	}

    public List<Map<String, Object>> findRelated(long categoryId, long productId) {
        return jdbc.queryForList("""
            SELECT product_id, name, slug, base_price, main_image
            FROM products
            WHERE category_id = ?
              AND product_id <> ?
              AND is_active = 1
            ORDER BY created_at DESC, product_id DESC
            LIMIT 4
        """, categoryId, productId);
    }
}
