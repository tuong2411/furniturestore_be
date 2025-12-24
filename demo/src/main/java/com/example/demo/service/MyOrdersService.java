package com.example.demo.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.springframework.stereotype.Service;

import com.example.demo.dto.MyOrderDto;
import com.example.demo.repository.OrderRepository;

@Service
public class MyOrdersService {

    private final OrderRepository repo;
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public MyOrdersService(OrderRepository repo) {
        this.repo = repo;
    }

    private String mapDbStatusToUi(String db) {
        if (db == null) return "Không rõ";
        return switch (db) {
            case "COMPLETED" -> "Đã giao";
            case "SHIPPING" -> "Đang giao";
            case "CANCELLED" -> "Đã hủy";
            case "CONFIRMED" -> "Đã xác nhận";
            case "PENDING" -> "Chờ xử lý";
            default -> db;
        };
    }

    private String toOrdCode(long orderId) {
        return "ORD" + String.format("%03d", orderId);
    }
    
    private String mapUiStatusToDb(String ui) {
        if (ui == null || ui.isBlank() || ui.equalsIgnoreCase("Tất cả")) return null;

        return switch (ui) {
            case "Đã giao" -> "COMPLETED";
            case "Đang giao" -> "SHIPPING";
            case "Đã hủy" -> "CANCELLED";
            default -> null;
        };
    }


    public List<MyOrderDto> getMyOrders(long userId, String uiStatus) {
    	String statusDb = mapUiStatusToDb(uiStatus);
        List<Map<String, Object>> rows = repo.findMyOrdersWithItems(userId,statusDb);

        Map<Long, MyOrderDto> byOrder = new LinkedHashMap<>();

        for (Map<String, Object> r : rows) {
            long orderId = ((Number) r.get("order_id")).longValue();

            MyOrderDto o = byOrder.get(orderId);
            if (o == null) {
                o = new MyOrderDto();
                o.orderId = orderId;
                o.id = toOrdCode(orderId);

                LocalDateTime created = (LocalDateTime) r.get("created_at");
                o.createdAt = created != null ? created.format(ISO) : null;

                o.total = (BigDecimal) r.get("total_amount");
                o.status = mapDbStatusToUi((String) r.get("order_status"));

                byOrder.put(orderId, o);
            }

            MyOrderDto.ItemDto it = new MyOrderDto.ItemDto();
            it.name = (String) r.get("product_name");
            it.quantity = ((Number) r.get("quantity")).intValue();
            it.price = (BigDecimal) r.get("unit_price");
            o.items.add(it);
        }

        return new ArrayList<>(byOrder.values());
    }
}
