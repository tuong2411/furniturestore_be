package com.example.demo.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.demo.dto.ConsultingRequestDto;


@Repository
public class ConsultingRepository {
    private final JdbcTemplate jdbc;

    public ConsultingRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long insert(Integer userId, String fullName, String phone, String email, String service, String message) {
        jdbc.update("""
            INSERT INTO consulting_requests(user_id, full_name, phone, email, service, message, status)
            VALUES(?, ?, ?, ?, ?, ?, 'NEW')
        """, userId, fullName, phone, email, service, message);

        Long id = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        return id == null ? 0L : id;
    }

    public Optional<ConsultingRequestDto> findById(long requestId) {
        List<ConsultingRequestDto> rows = jdbc.query("""
            SELECT *
            FROM consulting_requests
            WHERE request_id = ?
            LIMIT 1
        """, (rs, i) -> map(rs), requestId);

        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    public List<ConsultingRequestDto> list(String statusOrNull, int limit, int offset) {
        limit = Math.min(Math.max(limit, 1), 200);
        offset = Math.max(offset, 0);

        if (statusOrNull == null || statusOrNull.isBlank()) {
            return jdbc.query("""
                SELECT *
                FROM consulting_requests
                ORDER BY created_at DESC
                LIMIT ? OFFSET ?
            """, (rs, i) -> map(rs), limit, offset);
        }

        return jdbc.query("""
            SELECT *
            FROM consulting_requests
            WHERE status = ?
            ORDER BY created_at DESC
            LIMIT ? OFFSET ?
        """, (rs, i) -> map(rs), statusOrNull.trim().toUpperCase(), limit, offset);
    }

    public int updateStatus(long requestId, String status, String note) {
        return jdbc.update("""
            UPDATE consulting_requests
            SET status = ?, note = ?
            WHERE request_id = ?
        """, status, note, requestId);
    }

    private ConsultingRequestDto map(ResultSet rs) throws SQLException {
        ConsultingRequestDto d = new ConsultingRequestDto();
        d.requestId = rs.getLong("request_id");
        Object uid = rs.getObject("user_id");
        d.userId = (uid == null) ? null : ((Number) uid).intValue();

        d.fullName = rs.getString("full_name");
        d.phone = rs.getString("phone");
        d.email = rs.getString("email");
        d.service = rs.getString("service");
        d.message = rs.getString("message");
        d.status = rs.getString("status");
        d.note = rs.getString("note");

        d.createdAt = rs.getTimestamp("created_at").toLocalDateTime();
        d.updatedAt = rs.getTimestamp("updated_at").toLocalDateTime();
        return d;
    }
}
