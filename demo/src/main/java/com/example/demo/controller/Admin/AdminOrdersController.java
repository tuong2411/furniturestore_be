package com.example.demo.controller.Admin;

import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.admin.*;
import com.example.demo.service.AdminOrderService;

@RestController
@RequestMapping("/api/admin/orders")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class AdminOrdersController {

  private final AdminOrderService service;

  public AdminOrdersController(AdminOrderService service) {
    this.service = service;
  }

  // GET /api/admin/orders?q=&status=&paymentStatus=&paymentMethod=&page=0&size=10
  @GetMapping
  public ResponseEntity<?> list(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String paymentStatus,
      @RequestParam(required = false) String paymentMethod,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size
  ) {
    return ResponseEntity.ok(service.list(q, status, paymentStatus, paymentMethod, page, size));
  }

  // GET /api/admin/orders/{id}
  @GetMapping("/{orderId}")
  public ResponseEntity<?> detail(@PathVariable long orderId) {
    return ResponseEntity.ok(service.getDetail(orderId));
  }

  // PATCH /api/admin/orders/{id}/status  { "status": "CONFIRMED" }
  @PatchMapping("/{orderId}/status")
  public ResponseEntity<?> updateStatus(@PathVariable long orderId, @RequestBody UpdateOrderStatusRequest req) {
    service.updateStatus(orderId, req.status);
    return ResponseEntity.ok(Map.of("message", "OK"));
  }

  // PATCH /api/admin/orders/{id}/payment-status { "paymentStatus": "PAID" }
  @PatchMapping("/{orderId}/payment-status")
  public ResponseEntity<?> updatePaymentStatus(@PathVariable long orderId, @RequestBody UpdatePaymentStatusRequest req) {
    service.updatePaymentStatus(orderId, req.paymentStatus);
    return ResponseEntity.ok(Map.of("message", "OK"));
  }

  // PATCH /api/admin/orders/{id}/note { "note": "..." }
  @PatchMapping("/{orderId}/note")
  public ResponseEntity<?> updateNote(@PathVariable long orderId, @RequestBody UpdateOrderNoteRequest req) {
    service.updateNote(orderId, req.note);
    return ResponseEntity.ok(Map.of("message", "OK"));
  }

  // Basic error mapping (để FE dễ bắt)
  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<?> notFound(NoSuchElementException e) {
    return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<?> badRequest(IllegalArgumentException e) {
    return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
  }
}
