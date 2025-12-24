package com.example.demo.service;

import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;

@Component
public class GeminiClient {

  private final RestTemplate rest = new RestTemplate();

  @Value("${gemini.api.key}")
  private String apiKey;

  @Value("${gemini.model.primary:gemini-2.5-flash}")
  private String primaryModel;

  @Value("${gemini.model.fallback:gemini-1.5-flash}")
  private String fallbackModel;

  public String generate(String prompt) {
    // thử primary trước, nếu overload thì thử fallback
    String r1 = tryGenerateWithRetries(primaryModel, prompt);
    if (r1 != null) return r1;

    String r2 = tryGenerateWithRetries(fallbackModel, prompt);
    if (r2 != null) return r2;

    // cuối cùng: trả lời “offline mode” nhẹ nhàng
    return "Hiện tại hệ thống AI đang quá tải 😥 Bạn thử lại sau vài giây nha.";
  }

  private String tryGenerateWithRetries(String model, String prompt) {
    int maxAttempts = 3;
    long backoffMs = 400;

    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
      try {
        return callGemini(model, prompt);
      } catch (HttpServerErrorException e) {
        // retry cho 503/429
        if (e.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE || e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
          sleep(backoffMs);
          backoffMs *= 2;
          continue;
        }
        // lỗi 5xx khác -> thôi
        return null;
      } catch (ResourceAccessException e) {
        // timeout/network -> retry
        sleep(backoffMs);
        backoffMs *= 2;
      } catch (Exception e) {
        return null;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private String callGemini(String model, String prompt) {
    String url = "https://generativelanguage.googleapis.com/v1beta/models/"
        + model + ":generateContent?key=" + apiKey;

    Map<String, Object> body = Map.of(
      "contents", List.of(
        Map.of("role", "user", "parts", List.of(Map.of("text", prompt)))
      ),
      "generationConfig", Map.of(
        "temperature", 0.4,
        "maxOutputTokens", 500
      )
    );

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ResponseEntity<Map> res = rest.postForEntity(url, new HttpEntity<>(body, headers), Map.class);

    Map<String, Object> json = res.getBody();
    List<Map<String, Object>> candidates = (List<Map<String, Object>>) json.get("candidates");
    if (candidates == null || candidates.isEmpty()) return null;

    Map<String, Object> c0 = candidates.get(0);
    Map<String, Object> content = (Map<String, Object>) c0.get("content");
    List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
    if (parts == null || parts.isEmpty()) return null;

    Object text = parts.get(0).get("text");
    return text == null ? null : String.valueOf(text);
  }

  private void sleep(long ms) {
    try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
  }
}
	