package com.example.demo.controller;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.bind.annotation.*;

import com.example.demo.repository.OrderRepository;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.EmailService;

@RestController
@RequestMapping("/api/mock-payment")
public class MockPaymentController {

    private final OrderRepository orderRepo;
    private final EmailService emailService;

    
    public MockPaymentController(OrderRepository orderRepo, EmailService emailService) {
		super();
		this.orderRepo = orderRepo;
		this.emailService = emailService;
	}

	@ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, String> forbidden(SecurityException ex) {
        return Map.of("message", ex.getMessage());
    }

	private long currentUserId(Authentication auth) {
	    if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails principal)) {
	        throw new org.springframework.web.server.ResponseStatusException(
	            org.springframework.http.HttpStatus.UNAUTHORIZED, "UNAUTHORIZED"
	        );
	    }
	    return principal.getUser().getUserId();
	}

    @GetMapping("/order/{orderId}")
    public Map<String, Object> getOrderInfo(Authentication auth, @PathVariable long orderId) {

        long userId = currentUserId(auth);

        var o = orderRepo.findOrderByIdForUser(orderId, userId)
                .orElseThrow(() -> new SecurityException("ORDER_NOT_FOUND_OR_FORBIDDEN"));

        BigDecimal total = (BigDecimal) o.get("total_amount");

        return Map.of(
                "orderId", ((Number) o.get("order_id")).longValue(),
                "total", total, // FE formatCurrency dùng ok
                "status", String.valueOf(o.get("status")),
                "paymentStatus", String.valueOf(o.get("payment_status"))
        );
    }

    @PostMapping("/success")
    public Map<String, Object> success(Authentication auth, @RequestBody Map<String, Object> body) {

        long userId = currentUserId(auth);
        long orderId = ((Number) body.get("orderId")).longValue();
        String emailTo = ((CustomUserDetails) auth.getPrincipal()).getUser().getEmail();

        int updated = orderRepo.markPaidForUser(orderId, userId);
        if (updated == 0) throw new SecurityException("ORDER_NOT_FOUND_OR_FORBIDDEN");

        emailService.sendPaidEmail(orderId, emailTo);

        return Map.of("ok", true, "orderId", orderId, "result", "00");
    }



    @PostMapping("/fail")
    public Map<String, Object> fail(Authentication auth, @RequestBody Map<String, Object> body) {
        long userId = currentUserId(auth);

        Object raw = body.get("orderId");
        if (raw == null) throw new SecurityException("MISSING_ORDER_ID");

        long orderId = ((Number) raw).longValue();

        orderRepo.findOrderByIdForUser(orderId, userId)
                .orElseThrow(() -> new SecurityException("ORDER_NOT_FOUND_OR_FORBIDDEN"));

        return Map.of("ok", true, "orderId", orderId, "result", "99");
    }
}
