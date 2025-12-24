package com.example.demo.dto;

import java.util.List;

public class MyOrdersResponse {
    public List<MyOrderDto> orders;

    public MyOrdersResponse(List<MyOrderDto> orders) {
        this.orders = orders;
    }
}
