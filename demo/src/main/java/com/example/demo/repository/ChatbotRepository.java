package com.example.demo.repository;

import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ChatbotRepository {
  private final JdbcTemplate jdbc;
  public ChatbotRepository(JdbcTemplate jdbc){ this.jdbc = jdbc; }

  // Search sản phẩm: ưu tiên FULLTEXT, fallback LIKE nếu FULLTEXT không ra
  public List<Map<String,Object>> searchProducts(String q, int limit) {
    limit = Math.min(Math.max(limit, 1), 8);

    String keyword = (q == null) ? "" : q.trim();
    if (keyword.isBlank()) return List.of();

    // 1) FULLTEXT
    List<Map<String,Object>> ft = jdbc.queryForList("""
      SELECT product_id, name, slug, base_price, main_image, short_desc
      FROM products
      WHERE is_active = 1
        AND MATCH(name, short_desc, description) AGAINST (? IN NATURAL LANGUAGE MODE)
      ORDER BY MATCH(name, short_desc, description) AGAINST (? IN NATURAL LANGUAGE MODE) DESC
      LIMIT ?
    """, keyword, keyword, limit);

    if (!ft.isEmpty()) return ft;

    // 2) Fallback LIKE (để bắt các trường hợp “tủ quần áo” vs “tủ áo”)
    String like = "%" + keyword + "%";
    return jdbc.queryForList("""
      SELECT product_id, name, slug, base_price, main_image, short_desc
      FROM products
      WHERE is_active = 1
        AND (name LIKE ? OR slug LIKE ? OR short_desc LIKE ? OR description LIKE ?)
      ORDER BY product_id DESC
      LIMIT ?
    """, like, like, like, like, limit);
  }

  // Promotions: ok
  public List<Map<String,Object>> activePromotions(int limit) {
    limit = Math.min(Math.max(limit, 1), 5);
    return jdbc.queryForList("""
      SELECT code, description, discount_type, discount_value, max_discount, min_order_amount, start_date, end_date
      FROM promotions
      WHERE is_active = 1 AND NOW() BETWEEN start_date AND end_date
      ORDER BY end_date ASC
      LIMIT ?
    """, limit);
  }

  // Categories nổi bật: ok
  public List<Map<String,Object>> categories(int limit) {
    limit = Math.min(Math.max(limit, 1), 10);
    return jdbc.queryForList("""
      SELECT category_id, name, slug, description
      FROM categories
      WHERE is_active = 1
      ORDER BY display_order ASC
      LIMIT ?
    """, limit);
  }

  // Search categories: tăng khả năng match (LIKE name/slug/description)
  public List<Map<String,Object>> searchCategories(String q, int limit) {
    limit = Math.min(Math.max(limit, 1), 6);
    String keyword = (q == null) ? "" : q.trim();
    if (keyword.isBlank()) return List.of();

    String like = "%" + keyword + "%";
    return jdbc.queryForList("""
      SELECT category_id, name, slug
      FROM categories
      WHERE is_active = 1
        AND (name LIKE ? OR slug LIKE ? OR description LIKE ?)
      ORDER BY display_order ASC
      LIMIT ?
    """, like, like, like, limit);
  }
}
