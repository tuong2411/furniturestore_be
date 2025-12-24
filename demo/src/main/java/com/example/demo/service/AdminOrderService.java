package com.example.demo.service;

import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.admin.*;
import com.example.demo.repository.AdminOrderRepository;

@Service
public class AdminOrderService {

  private final AdminOrderRepository repo;

  // Bạn có thể đổi theo ENUM trong DB của bạn
  private static final Set<String> ORDER_STATUSES = Set.of(
      "NEW", "CONFIRMED", "SHIPPING", "COMPLETED", "CANCELLED"
  );

  private static final Set<String> PAYMENT_STATUSES = Set.of(
      "UNPAID", "PAID", "FAILED", "REFUNDED"
  );

  public AdminOrderService(AdminOrderRepository repo) {
    this.repo = repo;
  }

  public Map<String, Object> list(String q, String status, String paymentStatus, String paymentMethod, int page, int size) {
    if (page < 0) page = 0;
    if (size <= 0) size = 10;
    if (size > 50) size = 50;

    long total = repo.countOrders(q, status, paymentStatus, paymentMethod);
    List<AdminOrderRowDto> items = repo.findOrders(q, status, paymentStatus, paymentMethod, page, size);

    return Map.of(
        "page", page,
        "size", size,
        "total", total,
        "items", items
    );
  }

  public AdminOrderDetailDto getDetail(long orderId) {
    return repo.findOrderDetail(orderId).orElseThrow(() -> new NoSuchElementException("ORDER_NOT_FOUND"));
  }

  @Transactional
  public void updateStatus(long orderId, String nextStatus) {
    if (nextStatus == null || nextStatus.isBlank()) throw new IllegalArgumentException("STATUS_REQUIRED");
    nextStatus = nextStatus.trim();

    if (!ORDER_STATUSES.contains(nextStatus)) throw new IllegalArgumentException("INVALID_STATUS");

    Map<String, Object> cur = repo.findOrderStatusAndPayment(orderId)
        .orElseThrow(() -> new NoSuchElementException("ORDER_NOT_FOUND"));

    String currentStatus = String.valueOf(cur.get("status"));

    // rule cơ bản: COMPLETED/CANCELLED không cho đổi nữa
    if ("COMPLETED".equals(currentStatus) || "CANCELLED".equals(currentStatus)) {
      throw new IllegalArgumentException("ORDER_FINALIZED");
    }

    // rule: không cho quay ngược đơn giản
    List<String> flow = List.of("NEW", "CONFIRMED", "SHIPPING", "COMPLETED");
    int curIdx = flow.indexOf(currentStatus);
    int nextIdx = flow.indexOf(nextStatus);

    if (!"CANCELLED".equals(nextStatus) && curIdx != -1 && nextIdx != -1 && nextIdx < curIdx) {
      throw new IllegalArgumentException("CANNOT_ROLLBACK_STATUS");
    }

    int updated = repo.updateOrderStatus(orderId, nextStatus);
    if (updated == 0) throw new NoSuchElementException("ORDER_NOT_FOUND");
  }

  @Transactional
  public void updatePaymentStatus(long orderId, String nextPaymentStatus) {
    if (nextPaymentStatus == null || nextPaymentStatus.isBlank()) throw new IllegalArgumentException("PAYMENT_STATUS_REQUIRED");
    nextPaymentStatus = nextPaymentStatus.trim();

    if (!PAYMENT_STATUSES.contains(nextPaymentStatus)) throw new IllegalArgumentException("INVALID_PAYMENT_STATUS");

    int updated = repo.updatePaymentStatus(orderId, nextPaymentStatus);
    if (updated == 0) throw new NoSuchElementException("ORDER_NOT_FOUND");
  }

  @Transactional
  public void updateNote(long orderId, String note) {
    int updated = repo.updateOrderNote(orderId, note == null ? null : note.trim());
    if (updated == 0) throw new NoSuchElementException("ORDER_NOT_FOUND");
  }
}
