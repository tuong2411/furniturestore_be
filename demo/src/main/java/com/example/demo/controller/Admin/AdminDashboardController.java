package com.example.demo.controller.Admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.service.AdminDashboardService;

@RestController
@RequestMapping("/api/admin/dashboard")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class AdminDashboardController {

  private final AdminDashboardService service;

  public AdminDashboardController(AdminDashboardService service) {
    this.service = service;
  }

  @GetMapping
  public ResponseEntity<?> dashboard() {
    return ResponseEntity.ok(service.getDashboard());
  }
}
