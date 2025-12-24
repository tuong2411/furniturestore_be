package com.example.demo.dto;

import java.time.LocalDateTime;

public class AddressDto {
	public long addressId;
    public String fullName;
    public String phone;
    public String province;
    public String district;
    public String ward;
    public String street;
    public boolean isDefault;
    public LocalDateTime createdAt;

}
