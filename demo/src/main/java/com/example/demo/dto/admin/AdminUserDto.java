package com.example.demo.dto.admin;

import java.time.LocalDateTime;

public class AdminUserDto {
  public long userId;
  public long roleId;
  public String roleName;

  public String fullName;
  public String email;
  public String phone;

  public String status;
  public LocalDateTime createdAt;
  public LocalDateTime updatedAt;
}
