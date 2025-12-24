package com.example.demo.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.demo.repository.AdminUserRepository;

@Service
public class AdminUserService {

  private final AdminUserRepository repo;

  public AdminUserService(AdminUserRepository repo) {
    this.repo = repo;
  }

  private static final List<String> ALLOWED_STATUS = List.of("ACTIVE", "INACTIVE", "BANNED");

  public Map<String, Object> listUsers(String keyword, String status, Long roleId, int page, int size) {
    int safePage = Math.max(0, page);
    int safeSize = Math.min(100, Math.max(1, size));
    int offset = safePage * safeSize;

    long total = repo.countUsers(keyword, status, roleId);
    List<Map<String, Object>> items = repo.findUsers(keyword, status, roleId, safeSize, offset);

    Map<String, Object> resp = new HashMap<>();
    resp.put("items", items);
    resp.put("page", safePage);
    resp.put("size", safeSize);
    resp.put("total", total);
    resp.put("totalPages", (int) Math.ceil(total * 1.0 / safeSize));
    return resp;
  }

  public Map<String, Object> getUser(long userId) {
    return repo.findById(userId).orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));
  }

  public void updateStatus(long actorUserId, long targetUserId, String status) {
    if (status == null || !ALLOWED_STATUS.contains(status)) {
      throw new IllegalArgumentException("INVALID_STATUS");
    }
    // Chặn tự ban/khóa chính mình (an toàn)
    if (actorUserId == targetUserId && !"ACTIVE".equals(status)) {
      throw new IllegalArgumentException("CANNOT_CHANGE_SELF_STATUS");
    }
    int updated = repo.updateStatus(targetUserId, status);
    if (updated == 0) throw new IllegalArgumentException("USER_NOT_FOUND");
  }

  public void updateRole(long actorUserId, long targetUserId, long roleId) {
    if (!repo.roleExists(roleId)) {
      throw new IllegalArgumentException("ROLE_NOT_FOUND");
    }
    // Chặn tự đổi role (an toàn)
    if (actorUserId == targetUserId) {
      throw new IllegalArgumentException("CANNOT_CHANGE_SELF_ROLE");
    }
    int updated = repo.updateRole(targetUserId, roleId);
    if (updated == 0) throw new IllegalArgumentException("USER_NOT_FOUND");
  }

  public List<Map<String, Object>> listRoles() {
    return repo.listRoles();
  }
}
