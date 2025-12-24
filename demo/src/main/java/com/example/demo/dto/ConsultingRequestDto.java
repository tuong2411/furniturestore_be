package com.example.demo.dto;

import java.time.LocalDateTime;

public class ConsultingRequestDto {
	public long requestId;
    public Integer userId;
    public String fullName;
    public String phone;
    public String email;
    public String service;
    public String message;
    public String status;
    public String note;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;

}
