package com.example.demo.controller;
import org.springframework.http.HttpStatus;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.AddToCartRequest;
import com.example.demo.dto.CartDto;
import com.example.demo.dto.UpdateCartItemQtyRequest;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.CartService;

@RestController
@RequestMapping("/api/cart")

public class CartController {
    private final CartService service;

    public CartController(CartService service) {
        this.service = service;
    }

    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, String> forbidden(SecurityException ex) {
        return Map.of("message", ex.getMessage());
    }

    private long currentUserId(Authentication auth) {
        CustomUserDetails principal = (CustomUserDetails) auth.getPrincipal();
        return principal.getUser().getUserId();
    }

    @GetMapping
    public CartDto myCart(Authentication auth) {
        return service.getMyCart(currentUserId(auth));
    }

    @PostMapping("/items")
    public CartDto add(Authentication auth, @RequestBody AddToCartRequest req) {
        return service.addToCart(currentUserId(auth), req);
    }

    @PatchMapping("/items/{cartItemId}")
    public CartDto updateQty(Authentication auth,
                             @PathVariable long cartItemId,
                             @RequestBody UpdateCartItemQtyRequest req) {
        return service.updateQty(currentUserId(auth), cartItemId, req.quantity);
    }

    @DeleteMapping("/items/{cartItemId}")
    public CartDto remove(Authentication auth, @PathVariable long cartItemId) {
        return service.removeItem(currentUserId(auth), cartItemId);
    }
}
