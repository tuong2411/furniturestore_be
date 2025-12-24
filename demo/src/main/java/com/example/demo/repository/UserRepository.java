package com.example.demo.repository;

import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.demo.model.User;
import com.example.demo.repository.rowmapper.UserRowMapper;

@Repository
public class UserRepository {
	private final JdbcTemplate jdbc;

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public User findByEmailOrPhone(String emailOrUsername) {
        String sql = """
            SELECT u.*, r.role_name
            FROM users u
            JOIN roles r ON u.role_id = r.role_id
            WHERE u.email = ? OR u.phone = ?
            """;
        try {
            return jdbc.queryForObject(sql, new UserRowMapper(),
                    emailOrUsername, emailOrUsername);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    public int insertCustomer(String fullName, String email, String passwordHash) {
        String sql = """
            INSERT INTO users (role_id, full_name, email, phone, password_hash, avatar_url, status)
            VALUES (2, ?, ?, NULL, ?, NULL, 'ACTIVE')
            """;
        return jdbc.update(sql, fullName, email, passwordHash);
    }
    public List<String> findAdminEmails() {
        return jdbc.queryForList("""
            SELECT u.email
            FROM users u
            JOIN roles r ON u.role_id = r.role_id
            WHERE r.role_name = 'ADMIN'
              AND u.status = 'ACTIVE'
        """, String.class);
    }

}
