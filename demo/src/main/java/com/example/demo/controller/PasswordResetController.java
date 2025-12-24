// com.example.demo.controller.PasswordResetController
package com.example.demo.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.ForgotPasswordRequest;
import com.example.demo.dto.ResetPasswordRequest;
import com.example.demo.service.PasswordResetService;

@RestController
@RequestMapping("/api/auth")
public class PasswordResetController {

    private final PasswordResetService service;

    public PasswordResetController(PasswordResetService service) {
        this.service = service;
    }

    @PostMapping("/forgot-password")
    public Map<String, Object> forgot(@RequestBody ForgotPasswordRequest req) {
        service.sendOtp(req == null ? null : req.email);
        return Map.of("ok", true);
    }

    @PostMapping("/reset-password")
    public Map<String, Object> reset(@RequestBody ResetPasswordRequest req) {
        if (req == null) throw new IllegalArgumentException("MISSING_BODY");
        service.resetPassword(req.email, req.otp, req.newPassword);
        return Map.of("ok", true);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> bad(IllegalArgumentException ex) {
        return Map.of("message", ex.getMessage());
    }
}
