package com.example.demo.repository;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.demo.dto.CategoryTreeDto;

@Repository
public class CategoryRepository {

	private final JdbcTemplate jdbcTemplate;

    public CategoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<CategoryRow> findAllActive() {
        String sql = """
            SELECT category_id, parent_id, name, slug, description, display_order
            FROM categories
            WHERE is_active = 1
            ORDER BY display_order ASC, category_id ASC
        """;

        return jdbcTemplate.query(sql, (ResultSet rs, int rowNum) -> {
            CategoryRow r = new CategoryRow();
            r.categoryId = rs.getLong("category_id");
            Object pid = rs.getObject("parent_id");
            r.parentId = (pid == null) ? null : ((Number) pid).longValue();
            r.name = rs.getString("name");
            r.slug = rs.getString("slug");
            r.description = rs.getString("description");
            Object d = rs.getObject("display_order");
            r.displayOrder = (d == null) ? 0 : ((Number) d).intValue();
            return r;
        });
    }
    public List<Map<String, Object>> listAllForDropdown() {
        String sql = """
          SELECT category_id, name
          FROM categories
          WHERE is_active = 1
          ORDER BY display_order ASC, category_id ASC
        """;
        return jdbcTemplate.queryForList(sql);
      }
    public static class CategoryRow {
        public long categoryId;
        public Long parentId;
        public String name;
        public String slug;
        public String description;
        public int displayOrder;

        public CategoryTreeDto toDto() {
            return new CategoryTreeDto(categoryId, name, slug, description, displayOrder);
        }
    }
}
