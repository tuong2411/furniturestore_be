package com.example.demo.service;

import com.example.demo.dto.ChatbotDtos.ProductCard;
import com.example.demo.repository.ChatbotRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class ChatbotService {

  private final ChatbotRepository repo;
  private final GeminiClient gemini;

  public ChatbotService(ChatbotRepository repo, GeminiClient gemini) {
    this.repo = repo;
    this.gemini = gemini;
  }

  public Result askPublicWithProducts(String userMessage) {
    String q = (userMessage == null) ? "" : userMessage.trim();
    if (q.isBlank()) return new Result("Bạn muốn hỏi về sản phẩm nào ạ? (vd: sofa, tủ quần áo, bàn ăn…)", List.of());

    // lấy sản phẩm match để FE render nút
    List<Map<String, Object>> rawProducts = repo.searchProducts(q, 4);
    List<ProductCard> products = toCards(rawProducts);

    // context ngắn, không đưa link
    String context = buildContextNoLinks(rawProducts);

    String prompt = """
Bạn là chatbot hỗ trợ cửa hàng nội thất.
CHỈ dùng dữ liệu trong CONTEXT để trả lời. KHÔNG được đưa bất kỳ đường link nào (không /products, không /categories, không http).

YÊU CẦU:
- Trả lời ngắn gọn 2–6 dòng, đúng trọng tâm.
- Nếu người dùng hỏi “có bán X không”: trả lời Có/Không + gợi ý 1-3 sản phẩm nếu có.
- Nếu thiếu thông tin: hỏi lại 1 câu (kích thước, màu, khoảng giá).
- Không bịa.

CONTEXT:
%s

USER QUESTION:
%s
""".formatted(context, q);

    String reply = gemini.generate(prompt);

    // Nếu AI quá tải / reply rỗng => fallback text theo DB
    if (reply == null || reply.isBlank()) {
      reply = products.isEmpty()
        ? "Mình chưa thấy sản phẩm phù hợp trong hệ thống. Bạn mô tả rõ hơn (khoảng giá / kích thước / chất liệu) nhé."
        : "Mình gợi ý một vài mẫu phù hợp bên dưới nha.";
    }

    return new Result(reply, products);
  }

  private String buildContextNoLinks(List<Map<String, Object>> products) {
    StringBuilder sb = new StringBuilder();
    sb.append("PRODUCTS_MATCH:\n");
    if (products == null || products.isEmpty()) {
      sb.append("- (none)\n");
      return sb.toString();
    }
    for (Map<String, Object> p : products) {
      sb.append("- name: ").append(nvl(p.get("name")))
        .append(", price: ").append(nvl(p.get("base_price")))
        .append(", short: ").append(nvl(p.get("short_desc")))
        .append("\n");
    }
    return sb.toString();
  }

  private List<ProductCard> toCards(List<Map<String, Object>> rows) {
    if (rows == null) return List.of();
    List<ProductCard> list = new ArrayList<>();
    for (Map<String, Object> r : rows) {
      Long id = r.get("product_id") == null ? null : ((Number) r.get("product_id")).longValue();
      String name = (String) r.get("name");
      String slug = (String) r.get("slug");
      BigDecimal price = (BigDecimal) r.get("base_price");
      String img = (String) r.get("main_image");
      if (slug != null && name != null) {
        list.add(new ProductCard(id, name, slug, price, img));
      }
    }
    return list;
  }

  private String nvl(Object o) { return o == null ? "" : String.valueOf(o); }

  public static class Result {
    public final String reply;
    public final List<ProductCard> products;
    public Result(String reply, List<ProductCard> products) {
      this.reply = reply;
      this.products = products;
    }
  }
}
