package com.example.demo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.AddToCartRequest;
import com.example.demo.dto.CartDto;
import com.example.demo.dto.UpdateCartItemQtyRequest;
import com.example.demo.service.CartService;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService service;

    public CartController(CartService service) {
        this.service = service;
    }

    // Bạn cần hàm lấy userId từ Authentication (tùy hệ login của bạn)
    private long currentUserId(Authentication auth) {
        // CÁCH 1 (nếu principal lưu userId dạng số):
        // return ((MyUserPrincipal) auth.getPrincipal()).getUserId();

        // CÁCH 2 (demo tạm): nếu auth.getName() là user_id
        return Long.parseLong(auth.getName());
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
