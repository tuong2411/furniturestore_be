
package com.example.demo.repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PasswordResetRepository {

    private final JdbcTemplate jdbc;

    public PasswordResetRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<Map<String, Object>> findUserByEmail(String email) {
        String sql = """
            SELECT user_id, email, full_name, status
            FROM users
            WHERE email = ?
            LIMIT 1
        """;
        List<Map<String, Object>> rows = jdbc.queryForList(sql, email);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    public void insertOtp(long userId, String otp, LocalDateTime expiresAt) {
        jdbc.update("""
            INSERT INTO password_reset_otp(user_id, otp_code, expires_at, used)
            VALUES (?, ?, ?, 0)
        """, userId, otp, Timestamp.valueOf(expiresAt));
    }

    public Optional<Long> findValidOtpId(long userId, String otp) {
        String sql = """
            SELECT id
            FROM password_reset_otp
            WHERE user_id = ?
              AND otp_code = ?
              AND used = 0
              AND expires_at >= NOW()
            ORDER BY id DESC
            LIMIT 1
        """;
        List<Long> ids = jdbc.queryForList(sql, Long.class, userId, otp);
        return ids.isEmpty() ? Optional.empty() : Optional.of(ids.get(0));
    }

    public void markOtpUsed(long otpId) {
        jdbc.update("UPDATE password_reset_otp SET used = 1 WHERE id = ?", otpId);
    }

    public int updatePasswordHash(long userId, String passwordHash) {

        return jdbc.update("""
            UPDATE users
            SET password_hash = ?
            WHERE user_id = ?
        """, passwordHash, userId);
    }

    public void cleanupOldOtps(long userId) {
        jdbc.update("""
            DELETE FROM password_reset_otp
            WHERE user_id = ?
              AND (expires_at < NOW() OR used = 1)
        """, userId);
    }
}
