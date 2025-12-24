package com.example.demo.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.ConsultingRequestDto;
import com.example.demo.dto.CreateConsultingRequest;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.ConsultingService;

@RestController
public class ConsultingController {

    private final ConsultingService service;

    public ConsultingController(ConsultingService service) {
        this.service = service;
    }

    private Integer currentUserIdOrNull(Authentication auth) {
        if (auth == null) return null;
        if (auth.getPrincipal() instanceof CustomUserDetails p) {
            return (int) p.getUser().getUserId();
        }
        return null;
    }

    @PostMapping("/api/consulting-requests")
    public ConsultingRequestDto create(Authentication auth, @RequestBody CreateConsultingRequest req) {
        Integer userId = currentUserIdOrNull(auth); // có thể null
        return service.create(userId, req);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> bad(IllegalArgumentException ex) {
        return Map.of("message", ex.getMessage());
    }
}
