package com.example.demo.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.example.demo.dto.OrderDetailDto;
import com.example.demo.repository.OrderRepository;

@Service
public class OrderService {
    private final OrderRepository repo;

    public OrderService(OrderRepository repo) {
        this.repo = repo;
    }

    public OrderDetailDto getMyOrderDetail(long userId, long orderId) {
        Map<String, Object> h = repo.findOrderHeaderForUser(orderId, userId)
                .orElseThrow(() -> new NoSuchElementException("ORDER_NOT_FOUND"));

        OrderDetailDto dto = new OrderDetailDto();
        dto.orderId = ((Number) h.get("order_id")).longValue();

        dto.status = (String) h.get("status");
        dto.paymentStatus = (String) h.get("payment_status");
        dto.paymentMethod = (String) h.get("payment_method");

        dto.subtotal = (BigDecimal) h.get("subtotal_amount");
        dto.discount = (BigDecimal) h.get("discount_amount");
        dto.shippingFee = (BigDecimal) h.get("shipping_fee");
        dto.total = (BigDecimal) h.get("total_amount");

        dto.promotionCode = (String) h.get("promotion_code");
        dto.createdAt = (LocalDateTime) h.get("created_at");

        OrderDetailDto.ShippingInfoDto s = new OrderDetailDto.ShippingInfoDto();
        s.fullName = (String) h.get("full_name");
        s.phone = (String) h.get("phone");
        s.city = (String) h.get("province");
        s.district = (String) h.get("district");
        s.ward = (String) h.get("ward");
        s.address = (String) h.get("street");
        dto.shippingInfo = s;

        List<Map<String, Object>> items = repo.findOrderItems(orderId);
        dto.items = items.stream().map(r -> {
            OrderDetailDto.ItemDto it = new OrderDetailDto.ItemDto();
            it.name = (String) r.get("product_name");
            it.variantInfo = (String) r.get("variant_info");
            it.quantity = ((Number) r.get("quantity")).intValue();
            it.unitPrice = (BigDecimal) r.get("unit_price");
            it.lineTotal = (BigDecimal) r.get("total_price");
            return it;
        }).toList();

        return dto;
    }
}
