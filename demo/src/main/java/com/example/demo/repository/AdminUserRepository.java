package com.example.demo.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AdminUserRepository {

  private final JdbcTemplate jdbc;

  public AdminUserRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<Map<String, Object>> findUsers(String keyword, String status, Long roleId, int limit, int offset) {
    String sql = """
      SELECT
        u.user_id, u.role_id, r.role_name,
        u.full_name, u.email, u.phone,
        u.status, u.created_at, u.updated_at
      FROM users u
      JOIN roles r ON r.role_id = u.role_id
      WHERE ( ? IS NULL OR ? = '' OR u.full_name LIKE CONCAT('%%', ?, '%%') OR u.email LIKE CONCAT('%%', ?, '%%') OR u.phone LIKE CONCAT('%%', ?, '%%') )
        AND ( ? IS NULL OR ? = '' OR u.status = ? )
        AND ( ? IS NULL OR u.role_id = ? )
      ORDER BY u.created_at DESC, u.user_id DESC
      LIMIT ? OFFSET ?
    """;

    return jdbc.queryForList(
      sql,
      keyword, keyword, keyword, keyword, keyword,
      status, status, status,
      roleId, roleId,
      limit, offset
    );
  }

  public long countUsers(String keyword, String status, Long roleId) {
    String sql = """
      SELECT COUNT(*)
      FROM users u
      WHERE ( ? IS NULL OR ? = '' OR u.full_name LIKE CONCAT('%%', ?, '%%') OR u.email LIKE CONCAT('%%', ?, '%%') OR u.phone LIKE CONCAT('%%', ?, '%%') )
        AND ( ? IS NULL OR ? = '' OR u.status = ? )
        AND ( ? IS NULL OR u.role_id = ? )
    """;

    Long v = jdbc.queryForObject(
      sql,
      Long.class,
      keyword, keyword, keyword, keyword, keyword,
      status, status, status,
      roleId, roleId
    );
    return v == null ? 0 : v;
  }

  public Optional<Map<String, Object>> findById(long userId) {
    String sql = """
      SELECT
        u.user_id, u.role_id, r.role_name,
        u.full_name, u.email, u.phone,
        u.status, u.created_at, u.updated_at
      FROM users u
      JOIN roles r ON r.role_id = u.role_id
      WHERE u.user_id = ?
    """;
    List<Map<String, Object>> rows = jdbc.queryForList(sql, userId);
    return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
  }

  public int updateStatus(long userId, String status) {
    return jdbc.update("""
      UPDATE users SET status = ?
      WHERE user_id = ?
    """, status, userId);
  }

  public int updateRole(long userId, long roleId) {
    return jdbc.update("""
      UPDATE users SET role_id = ?
      WHERE user_id = ?
    """, roleId, userId);
  }

  public boolean roleExists(long roleId) {
    Long v = jdbc.queryForObject("SELECT COUNT(*) FROM roles WHERE role_id = ?", Long.class, roleId);
    return v != null && v > 0;
  }

  public List<Map<String, Object>> listRoles() {
    return jdbc.queryForList("""
      SELECT role_id, role_name, description
      FROM roles
      ORDER BY role_id ASC
    """);
  }
}
