package com.example.demo.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.example.demo.dto.admin.*;

@Repository
public class AdminOrderRepository {

  private final JdbcTemplate jdbc;

  public AdminOrderRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<AdminOrderRowDto> findOrders(
      String q,
      String status,
      String paymentStatus,
      String paymentMethod,
      int page,
      int size
  ) {

    StringBuilder sql = new StringBuilder("""
      SELECT
        o.order_id,
        o.created_at,
        o.user_id,
        u.full_name AS customer_name,
        u.email AS customer_email,
        o.total_amount,
        o.status,
        o.payment_status,
        o.payment_method
      FROM orders o
      JOIN users u ON u.user_id = o.user_id
      WHERE 1=1
    """);

    List<Object> args = new ArrayList<>();

    if (q != null && !q.isBlank()) {
      sql.append("""
        AND (
          CAST(o.order_id AS CHAR) LIKE ?
          OR u.full_name LIKE ?
          OR u.email LIKE ?
        )
      """);
      String like = "%" + q.trim() + "%";
      args.add(like);
      args.add(like);
      args.add(like);
    }

    if (status != null && !status.isBlank()) {
      sql.append(" AND o.status = ? ");
      args.add(status.trim());
    }

    if (paymentStatus != null && !paymentStatus.isBlank()) {
      sql.append(" AND o.payment_status = ? ");
      args.add(paymentStatus.trim());
    }

    if (paymentMethod != null && !paymentMethod.isBlank()) {
      sql.append(" AND o.payment_method = ? ");
      args.add(paymentMethod.trim());
    }

    sql.append(" ORDER BY o.created_at DESC, o.order_id DESC ");
    sql.append(" LIMIT ? OFFSET ? ");
    args.add(size);
    args.add(page * size);

    return jdbc.query(sql.toString(), args.toArray(), new RowMapper<AdminOrderRowDto>() {
      @Override public AdminOrderRowDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        AdminOrderRowDto dto = new AdminOrderRowDto();
        dto.orderId = rs.getLong("order_id");
        dto.createdAt = rs.getTimestamp("created_at").toLocalDateTime();
        dto.userId = rs.getLong("user_id");
        dto.customerName = rs.getString("customer_name");
        dto.customerEmail = rs.getString("customer_email");
        dto.totalAmount = rs.getBigDecimal("total_amount");
        dto.status = rs.getString("status");
        dto.paymentStatus = rs.getString("payment_status");
        dto.paymentMethod = rs.getString("payment_method");
        return dto;
      }
    });
  }

  public long countOrders(String q, String status, String paymentStatus, String paymentMethod) {
    StringBuilder sql = new StringBuilder("""
      SELECT COUNT(*) 
      FROM orders o
      JOIN users u ON u.user_id = o.user_id
      WHERE 1=1
    """);

    List<Object> args = new ArrayList<>();

    if (q != null && !q.isBlank()) {
      sql.append("""
        AND (
          CAST(o.order_id AS CHAR) LIKE ?
          OR u.full_name LIKE ?
          OR u.email LIKE ?
        )
      """);
      String like = "%" + q.trim() + "%";
      args.add(like);
      args.add(like);
      args.add(like);
    }
    if (status != null && !status.isBlank()) { sql.append(" AND o.status = ? "); args.add(status.trim()); }
    if (paymentStatus != null && !paymentStatus.isBlank()) { sql.append(" AND o.payment_status = ? "); args.add(paymentStatus.trim()); }
    if (paymentMethod != null && !paymentMethod.isBlank()) { sql.append(" AND o.payment_method = ? "); args.add(paymentMethod.trim()); }

    Long n = jdbc.queryForObject(sql.toString(), args.toArray(), Long.class);
    return n == null ? 0 : n;
  }

  public Optional<AdminOrderDetailDto> findOrderDetail(long orderId) {
    String headerSql = """
      SELECT
        o.order_id, o.created_at, o.updated_at,
        o.user_id,
        u.full_name AS customer_name,
        u.email AS customer_email,
        u.phone AS customer_phone,

        o.status, o.payment_status, o.payment_method,
        o.subtotal_amount, o.discount_amount, o.shipping_fee, o.total_amount,
        o.promotion_code,
        o.order_note,
        o.address_id,

        a.full_name AS address_full_name,
        a.phone AS address_phone,
        a.province,
        a.district,
        a.ward,
        a.street
      FROM orders o
      JOIN users u ON u.user_id = o.user_id
      LEFT JOIN user_addresses a ON a.address_id = o.address_id
      WHERE o.order_id = ?
    """;

    List<AdminOrderDetailDto> rows = jdbc.query(headerSql, new Object[]{orderId}, (rs, i) -> {
      AdminOrderDetailDto dto = new AdminOrderDetailDto();
      dto.orderId = rs.getLong("order_id");
      dto.createdAt = rs.getTimestamp("created_at").toLocalDateTime();
      dto.updatedAt = rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null;

      dto.userId = rs.getLong("user_id");
      dto.customerName = rs.getString("customer_name");
      dto.customerEmail = rs.getString("customer_email");
      dto.customerPhone = rs.getString("customer_phone");

      dto.status = rs.getString("status");
      dto.paymentStatus = rs.getString("payment_status");
      dto.paymentMethod = rs.getString("payment_method");

      dto.subtotalAmount = rs.getBigDecimal("subtotal_amount");
      dto.discountAmount = rs.getBigDecimal("discount_amount");
      dto.shippingFee = rs.getBigDecimal("shipping_fee");
      dto.totalAmount = rs.getBigDecimal("total_amount");
      dto.promotionCode = rs.getString("promotion_code");
      dto.orderNote = rs.getString("order_note");

      long addrId = rs.getLong("address_id");
      dto.addressId = rs.wasNull() ? null : addrId;
      dto.addressFullName = rs.getString("address_full_name");
      dto.addressPhone = rs.getString("address_phone");
      dto.province = rs.getString("province");
      dto.district = rs.getString("district");
      dto.ward = rs.getString("ward");
      dto.street = rs.getString("street");

      return dto;
    });

    if (rows.isEmpty()) return Optional.empty();

    AdminOrderDetailDto dto = rows.get(0);
    dto.items = findOrderItems(orderId);
    return Optional.of(dto);
  }

  public List<AdminOrderItemDto> findOrderItems(long orderId) {
    String sql = """
      SELECT
        oi.order_item_id,
        oi.product_id,
        oi.variant_id,
        oi.product_name,
        oi.variant_info,
        oi.quantity,
        oi.unit_price,
        oi.total_price
      FROM order_items oi
      WHERE oi.order_id = ?
      ORDER BY oi.order_item_id ASC
    """;

    return jdbc.query(sql, new Object[]{orderId}, (rs, i) -> {
      AdminOrderItemDto it = new AdminOrderItemDto();
      it.orderItemId = rs.getLong("order_item_id");
      it.productId = rs.getLong("product_id");

      long vid = rs.getLong("variant_id");
      it.variantId = rs.wasNull() ? null : vid;

      it.productName = rs.getString("product_name");
      it.variantInfo = rs.getString("variant_info");
      it.quantity = rs.getInt("quantity");
      it.unitPrice = rs.getBigDecimal("unit_price");
      it.totalPrice = rs.getBigDecimal("total_price");
      return it;
    });
  }

  public int updateOrderStatus(long orderId, String status) {
    String sql = """
      UPDATE orders
      SET status = ?, updated_at = NOW()
      WHERE order_id = ?
    """;
    return jdbc.update(sql, status, orderId);
  }

  public int updatePaymentStatus(long orderId, String paymentStatus) {
    String sql = """
      UPDATE orders
      SET payment_status = ?, updated_at = NOW()
      WHERE order_id = ?
    """;
    return jdbc.update(sql, paymentStatus, orderId);
  }

  public int updateOrderNote(long orderId, String note) {
    String sql = """
      UPDATE orders
      SET order_note = ?, updated_at = NOW()
      WHERE order_id = ?
    """;
    return jdbc.update(sql, note, orderId);
  }

  public Optional<Map<String, Object>> findOrderStatusAndPayment(long orderId) {
    String sql = "SELECT status, payment_status FROM orders WHERE order_id = ?";
    List<Map<String, Object>> rows = jdbc.queryForList(sql, orderId);
    return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
  }
}
