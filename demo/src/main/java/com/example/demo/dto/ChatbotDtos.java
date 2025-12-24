package com.example.demo.dto;

import java.math.BigDecimal;
import java.util.List;

public class ChatbotDtos {
  public static class AskReq {
    public String message;
  }

  public static class ProductCard {
    public Long productId;
    public String name;
    public String slug;
    public BigDecimal price;
    public String image;

    public ProductCard(Long productId, String name, String slug, BigDecimal price, String image) {
      this.productId = productId;
      this.name = name;
      this.slug = slug;
      this.price = price;
      this.image = image;
    }
  }

  public static class AskRes {
    public String reply;
    public List<ProductCard> products;

    public AskRes(String reply, List<ProductCard> products) {
      this.reply = reply;
      this.products = products;
    }
  }
}
