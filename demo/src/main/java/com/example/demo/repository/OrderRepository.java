package com.example.demo.repository;

import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class OrderRepository {
    private final JdbcTemplate jdbc;

    public OrderRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ===== MY ORDERS (list) =====
    public List<Map<String, Object>> findMyOrdersWithItems(long userId, String statusDb) {
        String sql = """
            SELECT
              o.order_id,
              o.created_at,
              o.total_amount,
              o.status AS order_status,

              oi.order_item_id,
              oi.product_name,
              oi.quantity,
              oi.unit_price
            FROM orders o
            JOIN order_items oi ON oi.order_id = o.order_id
            WHERE o.user_id = ?
              AND (? IS NULL OR o.status = ?)
            ORDER BY o.created_at DESC, oi.order_item_id ASC
        """;
        return jdbc.queryForList(sql, userId, statusDb, statusDb);
    }

    // ===== ORDER DETAIL (success / detail page) =====
    public Optional<Map<String, Object>> findOrderHeaderForUser(long orderId, long userId) {
        String sql = """
            SELECT
              o.order_id,
              o.status,
              o.payment_status,
              o.payment_method,
              o.subtotal_amount,
              o.discount_amount,
              o.shipping_fee,
              o.total_amount,
              o.promotion_code,
              o.created_at,

              a.full_name,
              a.phone,
              a.province,
              a.district,
              a.ward,
              a.street
            FROM orders o
            JOIN user_addresses a ON a.address_id = o.address_id
            WHERE o.order_id = ? AND o.user_id = ?
            LIMIT 1
        """;
        List<Map<String, Object>> rows = jdbc.queryForList(sql, orderId, userId);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    public List<Map<String, Object>> findOrderItems(long orderId) {
        String sql = """
            SELECT
              product_name,
              variant_info,
              quantity,
              unit_price,
              total_price
            FROM order_items
            WHERE order_id = ?
            ORDER BY order_item_id ASC
        """;
        return jdbc.queryForList(sql, orderId);
    }

    // ===== VNPay needs =====
    public Optional<Map<String, Object>> findOrderByIdForUser(long orderId, long userId) {
        String sql = """
            SELECT order_id, user_id, total_amount, payment_status, status
            FROM orders
            WHERE order_id = ? AND user_id = ?
            LIMIT 1
        """;
        List<Map<String, Object>> rows = jdbc.queryForList(sql, orderId, userId);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    public int markPaidForUser(long orderId, long userId) {
        String sql = """
            UPDATE orders
            SET payment_status = 'PAID',
                payment_method = 'VNPAY',
                updated_at = CURRENT_TIMESTAMP
            WHERE order_id = ? AND user_id = ?
        """;
        return jdbc.update(sql, orderId, userId);
    }
    public Optional<Map<String, Object>> findOrderEmailHeader(long orderId) {
        String sql = """
            SELECT o.order_id, o.total_amount, o.payment_method, o.order_note,
                   a.full_name, a.phone, a.province, a.district, a.ward, a.street,
                   u.email
            FROM orders o
            JOIN user_addresses a ON a.address_id = o.address_id
            JOIN users u ON u.user_id = o.user_id
            WHERE o.order_id = ?
            LIMIT 1
        """;
        List<Map<String, Object>> rows = jdbc.queryForList(sql, orderId);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    public List<Map<String, Object>> findOrderEmailItems(long orderId) {
        String sql = """
            SELECT product_name, variant_info, quantity, unit_price
            FROM order_items
            WHERE order_id = ?
            ORDER BY order_item_id ASC
        """;
        return jdbc.queryForList(sql, orderId);
    }

}
