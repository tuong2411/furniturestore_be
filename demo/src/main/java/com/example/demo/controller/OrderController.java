package com.example.demo.controller;

import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.OrderDetailDto;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.OrderService;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    private long currentUserId(Authentication auth) {
        CustomUserDetails principal = (CustomUserDetails) auth.getPrincipal();
        return principal.getUser().getUserId();
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> notFound(NoSuchElementException ex) {
        return Map.of("message", ex.getMessage());
    }

    @GetMapping("/{orderId}")
    public OrderDetailDto myOrderDetail(Authentication auth, @PathVariable long orderId) {
        return service.getMyOrderDetail(currentUserId(auth), orderId);
    }
}
