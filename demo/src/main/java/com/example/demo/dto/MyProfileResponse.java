package com.example.demo.dto;

import java.time.LocalDateTime;

public class MyProfileResponse {

	public long userId;
    public String fullName;
    public String email;
    public String phone;
    public String avatarUrl;
    public String status;
    public String role;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}
