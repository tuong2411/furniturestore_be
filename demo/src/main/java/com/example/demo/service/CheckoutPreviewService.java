package com.example.demo.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.example.demo.dto.CheckoutPreviewRequest;
import com.example.demo.dto.CheckoutPreviewResponse;
import com.example.demo.repository.CheckoutRepository;
import com.example.demo.repository.CheckoutRepository.CheckoutItemRow;
import com.example.demo.repository.CheckoutRepository.PromotionRow;

@Service
public class CheckoutPreviewService {

    private final CheckoutRepository repo;

    public CheckoutPreviewService(CheckoutRepository repo) {
        this.repo = repo;
    }

    public CheckoutPreviewResponse preview(long userId, CheckoutPreviewRequest req) {
        if (req == null || req.cartItemIds == null || req.cartItemIds.isEmpty()) {
            throw new IllegalArgumentException("EMPTY_ITEMS");
        }

        long cartId = repo.findActiveCartId(userId)
                .orElseThrow(() -> new NoSuchElementException("CART_NOT_FOUND"));

        List<CheckoutItemRow> items = repo.findSelectedItems(cartId, req.cartItemIds);
        if (items.size() != req.cartItemIds.size()) {
            throw new SecurityException("FORBIDDEN");
        }

        // subtotal (server-side)
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CheckoutItemRow it : items) {
            BigDecimal line = it.unitPrice.multiply(BigDecimal.valueOf(it.quantity));
            subtotal = subtotal.add(line);
        }

        // shipping fee rule giống FE
        BigDecimal shippingFee = subtotal.compareTo(BigDecimal.valueOf(500000)) > 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(30000);

        // promo
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

        CheckoutPreviewResponse res = new CheckoutPreviewResponse();
        res.subtotal = subtotal;
        res.discount = discount;
        res.shippingFee = shippingFee;
        res.total = total;
        res.promotionCode = promoCode;
        return res;
    }
}
