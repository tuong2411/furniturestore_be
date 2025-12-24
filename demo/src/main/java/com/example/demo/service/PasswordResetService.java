
package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.repository.PasswordResetRepository;

@Service
public class PasswordResetService {

    private final PasswordResetRepository repo;
    private final EmailService emailService;
    private final PasswordEncoder encoder;

    public PasswordResetService(PasswordResetRepository repo, EmailService emailService, PasswordEncoder encoder) {
        this.repo = repo;
        this.emailService = emailService;
        this.encoder = encoder;
    }

    @Transactional
    public void sendOtp(String email) {
        if (email == null || email.trim().isEmpty()) throw new IllegalArgumentException("MISSING_EMAIL");
        email = email.trim().toLowerCase();

        var uOpt = repo.findUserByEmail(email);
        if (uOpt.isEmpty()) return;

        Map<String, Object> u = uOpt.get();
        long userId = ((Number) u.get("user_id")).longValue();

        Object stObj = u.get("status");
        if (stObj != null && "INACTIVE".equalsIgnoreCase(String.valueOf(stObj))) return;

        String fullName = String.valueOf(u.get("full_name"));

        repo.cleanupOldOtps(userId);

        String otp = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
        int minutes = 10;
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(minutes);

        repo.insertOtp(userId, otp, expiresAt);

        emailService.sendResetOtpEmail(email, fullName, otp, minutes);
    }

    @Transactional
    public void resetPassword(String email, String otp, String newPassword) {
        if (email == null || email.trim().isEmpty()) throw new IllegalArgumentException("MISSING_EMAIL");
        if (otp == null || otp.trim().isEmpty()) throw new IllegalArgumentException("MISSING_OTP");
        if (newPassword == null || newPassword.trim().isEmpty()) throw new IllegalArgumentException("MISSING_NEW_PASSWORD");

        email = email.trim().toLowerCase();
        otp = otp.trim();

        if (newPassword.length() < 6) throw new IllegalArgumentException("PASSWORD_TOO_SHORT");

        var uOpt = repo.findUserByEmail(email);
        if (uOpt.isEmpty()) throw new IllegalArgumentException("INVALID_OTP");

        long userId = ((Number) uOpt.get().get("user_id")).longValue();

        Long otpId = repo.findValidOtpId(userId, otp)
            .orElseThrow(() -> new IllegalArgumentException("INVALID_OTP"));

        // dùng 1 lần
        repo.markOtpUsed(otpId);

        // đổi pass
        String hash = encoder.encode(newPassword);
        int ok = repo.updatePasswordHash(userId, hash);
        if (ok != 1) throw new IllegalStateException("RESET_FAILED");

        repo.cleanupOldOtps(userId);
    }
}
