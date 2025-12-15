package com.example.demo.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.CheckoutRequest;
import com.example.demo.dto.CheckoutResponse;
import com.example.demo.repository.CheckoutRepository;
import com.example.demo.repository.CheckoutRepository.CheckoutItemRow;
import com.example.demo.repository.CheckoutRepository.PromotionRow;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class CheckoutService {

    private final CheckoutRepository repo;
    private final EmailService emailService;


    public CheckoutService(CheckoutRepository repo, EmailService emailService) {
		super();
		this.repo = repo;
		this.emailService = emailService;
	}

	@Transactional
    public CheckoutResponse checkout(long userId, CheckoutRequest req) {

        if (req == null || req.cartItemIds == null || req.cartItemIds.isEmpty()) {
            throw new IllegalArgumentException("EMPTY_ITEMS");
        }
        if (req.shippingInfo == null) throw new IllegalArgumentException("MISSING_SHIPPING_INFO");

        var s = req.shippingInfo;

        String fullName = must(s.fullName, "fullName");
        String phone    = must(s.phone, "phone");
        String street   = must(s.address, "address");
        String province = must(s.city, "city");

        // DB user_addresses district/ward NOT NULL :contentReference[oaicite:7]{index=7}
        String district = nonEmptyOr(s.district, "N/A");
        String ward     = nonEmptyOr(s.ward, "N/A");

        long cartId = repo.findActiveCartId(userId)
                .orElseThrow(() -> new NoSuchElementException("CART_NOT_FOUND"));

        // 1) Load selected items (must belong to this cart)
        List<CheckoutItemRow> items = repo.findSelectedItems(cartId, req.cartItemIds);
        if (items.size() != req.cartItemIds.size()) {
            throw new SecurityException("FORBIDDEN"); // cố tình chọn item của người khác
        }

        // 2) Stock check + subtotal
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CheckoutItemRow it : items) {
            if (it.quantity <= 0) throw new IllegalArgumentException("INVALID_QTY");

            if (it.variantId != null) {
                if (it.variantStock == null || it.variantStock < it.quantity) {
                    throw new IllegalStateException("OUT_OF_STOCK_VARIANT");
                }
            } else {
                if (it.productStock < it.quantity) {
                    throw new IllegalStateException("OUT_OF_STOCK_PRODUCT");
                }
            }

            BigDecimal line = it.unitPrice.multiply(BigDecimal.valueOf(it.quantity));
            subtotal = subtotal.add(line);
        }

        // 3) Shipping fee (y chang FE)
        BigDecimal shippingFee = subtotal.compareTo(BigDecimal.valueOf(500000)) > 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(30000);

        // 4) Promotion (optional)
        BigDecimal discount = BigDecimal.ZERO;
        String promoCode = (req.promotionCode == null || req.promotionCode.isBlank())
                ? null
                : req.promotionCode.trim();

        if (promoCode != null) {
            PromotionRow promo = repo.findPromotion(promoCode)
                    .orElseThrow(() -> new NoSuchElementException("PROMO_NOT_FOUND"));

            if (!promo.isActive) throw new IllegalArgumentException("PROMO_INACTIVE");

            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(promo.startDate) || now.isAfter(promo.endDate)) {
                throw new IllegalArgumentException("PROMO_EXPIRED");
            }

            if (subtotal.compareTo(promo.minOrderAmount) < 0) {
                throw new IllegalArgumentException("PROMO_MIN_NOT_MET");
            }

            if (promo.usageLimit != null && promo.usedCount >= promo.usageLimit) {
                throw new IllegalArgumentException("PROMO_USAGE_LIMIT");
            }

            if ("PERCENT".equalsIgnoreCase(promo.discountType)) {
                discount = subtotal.multiply(promo.discountValue).divide(BigDecimal.valueOf(100));
                if (promo.maxDiscount != null && discount.compareTo(promo.maxDiscount) > 0) {
                    discount = promo.maxDiscount;
                }
            } else { // AMOUNT
                discount = promo.discountValue;
            }

            if (discount.compareTo(subtotal) > 0) discount = subtotal;
        }

        BigDecimal total = subtotal.subtract(discount).add(shippingFee);

        // 5) Insert address snapshot
        long addressId = repo.insertAddress(userId, fullName, phone, province, district, ward, street);

        // 6) Insert order
        String paymentMethodEnum = mapPayment(req.paymentMethod);
        boolean hasNoteColumn = repo.ordersHasNoteColumn();

        long orderId = repo.insertOrder(
                userId, addressId, promoCode,
                subtotal, discount, shippingFee, total,
                paymentMethodEnum,
                (req.note == null || req.note.isBlank()) ? null : req.note.trim(),
                hasNoteColumn
        );

        // 7) Insert order_items snapshot
        for (CheckoutItemRow it : items) {
            repo.insertOrderItem(orderId, it);
        }

        // 8) Decrease stock (atomic check in SQL)
        for (CheckoutItemRow it : items) {
            int ok;
            if (it.variantId != null) {
                ok = repo.decreaseVariantStock(it.variantId, it.quantity);
                if (ok != 1) throw new IllegalStateException("OUT_OF_STOCK_VARIANT");
            } else {
                ok = repo.decreaseProductStock(it.productId, it.quantity);
                if (ok != 1) throw new IllegalStateException("OUT_OF_STOCK_PRODUCT");
            }
        }

        // 9) Increase promo used_count (atomic)
        if (promoCode != null) {
            int ok = repo.increasePromotionUsedCountSafely(promoCode);
            if (ok != 1) throw new IllegalStateException("PROMO_RACE_CONDITION");
        }

        // 10) Delete selected cart items
        repo.deleteCartItems(cartId, req.cartItemIds);

        CheckoutResponse res = new CheckoutResponse();
        res.orderId = orderId;
        res.subtotal = subtotal;
        res.discount = discount;
        res.shippingFee = shippingFee;
        res.total = total;
        res.status = "PENDING";
        res.paymentStatus = "UNPAID";
        res.paymentMethod = paymentMethodEnum;
        
        
        String emailTo = s.email; // lấy từ request shippingInfo.email
        String addressText = province + ", " + district + ", " + ward + " - " + street;

        // build items for email (nhẹ thôi)
        List<Map<String, Object>> emailItems = new ArrayList<>();
        for (CheckoutItemRow it : items) {
            Map<String, Object> m = new HashMap<>();
            m.put("name", it.productName);
            m.put("variantInfo", it.variantInfo);
            m.put("qty", it.quantity);
            m.put("priceFormatted", formatVnd(it.unitPrice));
            m.put("lineTotalFormatted", formatVnd(it.unitPrice.multiply(BigDecimal.valueOf(it.quantity))));
            emailItems.add(m);
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                emailService.sendOrderSuccessEmail(
                    emailTo,
                    orderId,
                    fullName,
                    phone,
                    addressText,
                    paymentMethodEnum,
                    req.note,
                    formatVnd(total),
                    emailItems
                );
            }
        });
        
        return res;
    }
    
    private String formatVnd(BigDecimal v) {
        java.text.NumberFormat nf = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("vi","VN"));
        nf.setMaximumFractionDigits(0);
        return nf.format(v == null ? BigDecimal.ZERO : v);
    }

    private String mapPayment(String pm) {
        if (pm == null) return "BANKING";
        return switch (pm.toLowerCase()) {
            case "cod" -> "COD";
            case "momo" -> "MOMO";
            case "bank" -> "BANKING";
            case "vnpay" -> "VNPAY";
            default -> "BANKING";
        };
    }

    private String must(String s, String field) {
        if (s == null || s.trim().isEmpty())
            throw new IllegalArgumentException("MISSING_" + field.toUpperCase());
        return s.trim();
    }

    private String nonEmptyOr(String s, String fallback) {
        return (s == null || s.trim().isEmpty()) ? fallback : s.trim();
    }
}
