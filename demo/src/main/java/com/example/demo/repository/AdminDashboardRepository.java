package com.example.demo.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AdminDashboardRepository {

  private final JdbcTemplate jdbc;

  public AdminDashboardRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public long countUsers() {
    Long v = jdbc.queryForObject("SELECT COUNT(*) FROM users", Long.class);
    return v == null ? 0 : v;
  }

  public long countActiveProducts() {
    Long v = jdbc.queryForObject("SELECT COUNT(*) FROM products WHERE is_active = 1", Long.class);
    return v == null ? 0 : v;
  }

  public long countOrders() {
    Long v = jdbc.queryForObject("SELECT COUNT(*) FROM orders", Long.class);
    return v == null ? 0 : v;
  }

  public long countOrdersToday() {
    Long v = jdbc.queryForObject("""
        SELECT COUNT(*)
        FROM orders
        WHERE DATE(created_at) = CURRENT_DATE
    """, Long.class);
    return v == null ? 0 : v;
  }

  /** Doanh thu: đơn PAID và không bị CANCELLED */
  public BigDecimal revenuePaidAllTime() {
    BigDecimal v = jdbc.queryForObject("""
        SELECT COALESCE(SUM(total_amount), 0)
        FROM orders
        WHERE payment_status = 'PAID'
          AND status <> 'CANCELLED'
    """, BigDecimal.class);
    return v == null ? BigDecimal.ZERO : v;
  }

  /** Doanh thu hôm nay: PAID */
  public BigDecimal revenuePaidToday() {
    BigDecimal v = jdbc.queryForObject("""
        SELECT COALESCE(SUM(total_amount), 0)
        FROM orders
        WHERE payment_status = 'PAID'
          AND status <> 'CANCELLED'
          AND DATE(created_at) = CURRENT_DATE
    """, BigDecimal.class);
    return v == null ? BigDecimal.ZERO : v;
  }

  /** Đếm đơn theo status (PENDING/CONFIRMED/SHIPPING/COMPLETED/CANCELLED) */
  public List<Map<String, Object>> countOrdersByStatus() {
    return jdbc.queryForList("""
        SELECT status, COUNT(*) AS total
        FROM orders
        GROUP BY status
        ORDER BY total DESC
    """);
  }

  /** Doanh thu 7 ngày gần nhất (PAID) */
  public List<Map<String, Object>> revenuePaidLast7Days() {
    return jdbc.queryForList("""
        SELECT
          DATE(created_at) AS day,
          COALESCE(SUM(total_amount), 0) AS revenue
        FROM orders
        WHERE created_at >= (CURRENT_DATE - INTERVAL 6 DAY)
          AND payment_status = 'PAID'
          AND status <> 'CANCELLED'
        GROUP BY DATE(created_at)
        ORDER BY day ASC
    """);
  }

  /** Top 5 sản phẩm bán chạy 30 ngày gần nhất (dựa order_items) */
  public List<Map<String, Object>> topProductsLast30Days() {
    return jdbc.queryForList("""
        SELECT
          oi.product_id,
          oi.product_name,
          SUM(oi.quantity) AS qty,
          COALESCE(SUM(oi.total_price), 0) AS gross
        FROM order_items oi
        JOIN orders o ON o.order_id = oi.order_id
        WHERE o.created_at >= (CURRENT_DATE - INTERVAL 29 DAY)
          AND o.payment_status = 'PAID'
          AND o.status <> 'CANCELLED'
        GROUP BY oi.product_id, oi.product_name
        ORDER BY qty DESC
        LIMIT 5
    """);
  }
}
