package com.example.demo.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.demo.repository.AdminDashboardRepository;

@Service
public class AdminDashboardService {

  private final AdminDashboardRepository repo;

  public AdminDashboardService(AdminDashboardRepository repo) {
    this.repo = repo;
  }

  public Map<String, Object> getDashboard() {
    long users = repo.countUsers();
    long activeProducts = repo.countActiveProducts();
    long orders = repo.countOrders();
    long ordersToday = repo.countOrdersToday();

    BigDecimal revenueAllTime = repo.revenuePaidAllTime();
    BigDecimal revenueToday = repo.revenuePaidToday();

    Map<String, Object> resp = new HashMap<>();
    resp.put("users", users);
    resp.put("activeProducts", activeProducts);
    resp.put("orders", orders);
    resp.put("ordersToday", ordersToday);

    resp.put("revenueAllTime", revenueAllTime);
    resp.put("revenueToday", revenueToday);

    resp.put("ordersByStatus", repo.countOrdersByStatus());
    resp.put("revenueLast7Days", repo.revenuePaidLast7Days());
    resp.put("topProductsLast30Days", repo.topProductsLast30Days());
    return resp;
  }
}
