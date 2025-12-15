package com.example.demo.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.CheckoutPreviewRequest;
import com.example.demo.dto.CheckoutPreviewResponse;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.CheckoutPreviewService;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutPreviewController {

    private final CheckoutPreviewService service;

    public CheckoutPreviewController(CheckoutPreviewService service) {
        this.service = service;
    }

    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, String> forbidden(SecurityException ex) {
        return Map.of("message", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> badRequest(IllegalArgumentException ex) {
        return Map.of("message", ex.getMessage());
    }

    private long currentUserId(Authentication auth) {
        CustomUserDetails principal = (CustomUserDetails) auth.getPrincipal();
        return principal.getUser().getUserId();
    }

    @PostMapping("/preview")
    public CheckoutPreviewResponse preview(Authentication auth, @RequestBody CheckoutPreviewRequest req) {
        return service.preview(currentUserId(auth), req);
    }
}
