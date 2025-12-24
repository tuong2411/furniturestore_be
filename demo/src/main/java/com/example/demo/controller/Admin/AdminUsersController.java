package com.example.demo.controller.Admin;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.admin.UpdateUserRoleRequest;
import com.example.demo.dto.admin.UpdateUserStatusRequest;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.AdminUserService;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class AdminUsersController {

  private final AdminUserService service;

  public AdminUsersController(AdminUserService service) {
    this.service = service;
  }

  private long currentUserId(Authentication auth) {
    CustomUserDetails principal = (CustomUserDetails) auth.getPrincipal();
    return principal.getUser().getUserId();
  }

  @GetMapping
  public ResponseEntity<?> list(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) Long roleId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size
  ) {
    return ResponseEntity.ok(service.listUsers(keyword, status, roleId, page, size));
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> detail(@PathVariable("id") long id) {
    return ResponseEntity.ok(service.getUser(id));
  }

  @PatchMapping("/{id}/status")
  public ResponseEntity<?> updateStatus(
      @PathVariable("id") long id,
      @RequestBody UpdateUserStatusRequest req,
      Authentication auth
  ) {
    long actorId = currentUserId(auth);
    service.updateStatus(actorId, id, req == null ? null : req.status);
    return ResponseEntity.ok(Map.of("message", "OK"));
  }

  @PatchMapping("/{id}/role")
  public ResponseEntity<?> updateRole(
      @PathVariable("id") long id,
      @RequestBody UpdateUserRoleRequest req,
      Authentication auth
  ) {
    long actorId = currentUserId(auth);
    service.updateRole(actorId, id, req == null ? 0 : req.roleId);
    return ResponseEntity.ok(Map.of("message", "OK"));
  }

  @GetMapping("/roles")
  public ResponseEntity<?> roles() {
    return ResponseEntity.ok(service.listRoles());
  }
}
