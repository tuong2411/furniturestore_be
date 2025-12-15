package com.example.demo.repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CheckoutRepository {
    private final JdbcTemplate jdbc;

    public CheckoutRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<Long> findActiveCartId(long userId) {
        String sql = """
            SELECT cart_id
            FROM carts
            WHERE user_id = ? AND status = 'ACTIVE'
            ORDER BY cart_id DESC
            LIMIT 1
        """;
        List<Long> ids = jdbc.queryForList(sql, Long.class, userId);
        return ids.isEmpty() ? Optional.empty() : Optional.of(ids.get(0));
    }

    // Load selected items + snapshot data (name + variant info) + stock
    public List<CheckoutItemRow> findSelectedItems(long cartId, List<Long> cartItemIds) {
        if (cartItemIds == null || cartItemIds.isEmpty()) return List.of();

        String inSql = cartItemIds.stream().map(x -> "?").collect(Collectors.joining(","));
        List<Object> args = new ArrayList<>();
        args.add(cartId);
        args.addAll(cartItemIds);
        String sql = """
            SELECT
              ci.cart_item_id,
              ci.product_id,
              ci.variant_id,
              ci.quantity,
              ci.unit_price,

              p.name AS product_name,
              p.base_stock AS product_stock,

              pv.stock AS variant_stock,
              CONCAT_WS(' | ', pv.material, pv.color, pv.size, pv.extra_desc) AS variant_info

            FROM cart_items ci
			    JOIN products p ON p.product_id = ci.product_id
			    LEFT JOIN product_variants pv ON pv.variant_id = ci.variant_id
			    WHERE ci.cart_id = ?
			      AND ci.cart_item_id IN (%s)
			    ORDER BY ci.cart_item_id DESC
			""".formatted(inSql);

        return jdbc.query(sql, (rs, i) -> mapItem(rs), args.toArray());
    }

    // Insert user_addresses snapshot, return address_id
    public long insertAddress(long userId, String fullName, String phone,
                              String province, String district, String ward, String street) {
        jdbc.update("""
            INSERT INTO user_addresses(user_id, full_name, phone, province, district, ward, street, is_default)
            VALUES(?, ?, ?, ?, ?, ?, ?, 0)
        """, userId, fullName, phone, province, district, ward, street);
        return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    public boolean ordersHasNoteColumn() {
        Integer c = jdbc.queryForObject("""
            SELECT COUNT(*)
            FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'orders'
              AND COLUMN_NAME = 'order_note'
        """, Integer.class);
        return c != null && c > 0;
    }

    // Insert order, return order_id
    public long insertOrder(long userId, long addressId, String promoCode,
                            BigDecimal subtotal, BigDecimal discount, BigDecimal shippingFee,
                            BigDecimal total, String paymentMethodEnum, String noteOrNull,
                            boolean hasNoteColumn) {
        if (hasNoteColumn) {
            jdbc.update("""
                INSERT INTO orders(
                  user_id, address_id, promotion_code,
                  subtotal_amount, discount_amount, shipping_fee, total_amount,
                  status, payment_status, payment_method, order_note
                ) VALUES (
                  ?, ?, ?,
                  ?, ?, ?, ?,
                  'PENDING', 'UNPAID', ?, ?
                )
            """, userId, addressId, promoCode, subtotal, discount, shippingFee, total, paymentMethodEnum, noteOrNull);
        } else {
            jdbc.update("""
                INSERT INTO orders(
                  user_id, address_id, promotion_code,
                  subtotal_amount, discount_amount, shipping_fee, total_amount,
                  status, payment_status, payment_method
                ) VALUES (
                  ?, ?, ?,
                  ?, ?, ?, ?,
                  'PENDING', 'UNPAID', ?
                )
            """, userId, addressId, promoCode, subtotal, discount, shippingFee, total, paymentMethodEnum);
        }
        return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    public void insertOrderItem(long orderId, CheckoutItemRow it) {
        BigDecimal totalPrice = it.unitPrice.multiply(BigDecimal.valueOf(it.quantity));

        jdbc.update("""
            INSERT INTO order_items(
              order_id, product_id, variant_id,
              product_name, variant_info,
              quantity, unit_price, total_price
            )
            VALUES(?, ?, ?, ?, ?, ?, ?, ?)
        """, orderId, it.productId, it.variantId, it.productName, it.variantInfo, it.quantity, it.unitPrice, totalPrice);
    }

    public int decreaseVariantStock(long variantId, int qty) {
        return jdbc.update("""
            UPDATE product_variants
            SET stock = stock - ?
            WHERE variant_id = ? AND stock >= ?
        """, qty, variantId, qty);
    }

    public int decreaseProductStock(long productId, int qty) {
        return jdbc.update("""
            UPDATE products
            SET base_stock = base_stock - ?
            WHERE product_id = ? AND base_stock >= ?
        """, qty, productId, qty);
    }

    // Promotions
    public Optional<PromotionRow> findPromotion(String code) {
        String sql = """
            SELECT
              code, discount_type, discount_value, max_discount, min_order_amount,
              start_date, end_date, usage_limit, used_count, is_active
            FROM promotions
            WHERE code = ?
            LIMIT 1
        """;
        List<PromotionRow> rows = jdbc.query(sql, (rs, i) -> mapPromo(rs), code);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    public int increasePromotionUsedCountSafely(String code) {
        // chống race condition: chỉ tăng nếu còn hiệu lực + còn lượt
        return jdbc.update("""
            UPDATE promotions
            SET used_count = used_count + 1
            WHERE code = ?
              AND is_active = 1
              AND NOW() BETWEEN start_date AND end_date
              AND (usage_limit IS NULL OR used_count < usage_limit)
        """, code);
    }

    // Delete selected cart_items
    public void deleteCartItems(long cartId, List<Long> cartItemIds) {
        if (cartItemIds == null || cartItemIds.isEmpty()) return;
        String inSql = cartItemIds.stream().map(x -> "?").collect(Collectors.joining(","));
        List<Object> args = new ArrayList<>();
        args.add(cartId);
        args.addAll(cartItemIds);

        jdbc.update("DELETE FROM cart_items WHERE cart_id = ? AND cart_item_id IN (" + inSql + ")", args.toArray());
    }


    private CheckoutItemRow mapItem(ResultSet rs) throws SQLException {
        CheckoutItemRow it = new CheckoutItemRow();
        it.cartItemId = rs.getLong("cart_item_id");
        it.productId = rs.getLong("product_id");
        it.variantId = (Long) rs.getObject("variant_id", Long.class);
        it.quantity = rs.getInt("quantity");
        it.unitPrice = rs.getBigDecimal("unit_price");

        it.productName = rs.getString("product_name");
        it.variantInfo = rs.getString("variant_info");

        it.productStock = rs.getInt("product_stock");
        it.variantStock = (Integer) rs.getObject("variant_stock", Integer.class);
        return it;
    }

    private PromotionRow mapPromo(ResultSet rs) throws SQLException {
        PromotionRow p = new PromotionRow();
        p.code = rs.getString("code");
        p.discountType = rs.getString("discount_type"); // PERCENT/AMOUNT :contentReference[oaicite:6]{index=6}
        p.discountValue = rs.getBigDecimal("discount_value");
        p.maxDiscount = (BigDecimal) rs.getObject("max_discount");
        p.minOrderAmount = rs.getBigDecimal("min_order_amount");
        p.startDate = rs.getTimestamp("start_date").toLocalDateTime();
        p.endDate = rs.getTimestamp("end_date").toLocalDateTime();
        p.usageLimit = (Integer) rs.getObject("usage_limit");
        p.usedCount = rs.getInt("used_count");
        Object isActiveObj = rs.getObject("is_active");
        p.isActive = (isActiveObj instanceof Boolean b) ? b : ((Number) isActiveObj).intValue() == 1;
        return p;
    }

    // ===== helper classes =====

    public static class CheckoutItemRow {
        public long cartItemId;
        public long productId;
        public Long variantId;

        public int quantity;
        public BigDecimal unitPrice;

        public String productName;
        public String variantInfo;

        public int productStock;
        public Integer variantStock;
    }

    public static class PromotionRow {
        public String code;
        public String discountType; // PERCENT or AMOUNT
        public BigDecimal discountValue;
        public BigDecimal maxDiscount;
        public BigDecimal minOrderAmount;
        public LocalDateTime startDate;
        public LocalDateTime endDate;
        public Integer usageLimit;
        public int usedCount;
        public boolean isActive;
    }
}
