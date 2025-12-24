package com.example.demo.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.MyOrderDto;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.MyOrdersService;

@RestController
@RequestMapping("/api/orders")
public class MyOrdersController {

    private final MyOrdersService service;

    public MyOrdersController(MyOrdersService service) {
        this.service = service;
    }

    private long currentUserId(Authentication auth) {
        CustomUserDetails principal = (CustomUserDetails) auth.getPrincipal();
        return principal.getUser().getUserId();
    }

    @GetMapping("/my")
    public List<MyOrderDto> myOrders(
            Authentication auth,
            @RequestParam(name = "status", required = false) String status
    ) {
        return service.getMyOrders(currentUserId(auth), status);
    }

}
