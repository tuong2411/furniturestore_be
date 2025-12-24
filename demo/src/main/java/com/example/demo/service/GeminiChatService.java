package com.example.demo.service;

import com.example.demo.dto.ChatbotRequest;

import tools.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;


@Service
public class GeminiChatService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String model;

    private final ObjectMapper om = new ObjectMapper();
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public String chat(ChatbotRequest req) {
        if (apiKey == null || apiKey.isBlank()) throw new IllegalStateException("GEMINI_API_KEY_MISSING");
        if (req == null || req.message == null || req.message.trim().isEmpty()) throw new IllegalArgumentException("MISSING_MESSAGE");

        List<Map<String, Object>> contents = new ArrayList<>();

        if (req.history != null) {
            for (var t : req.history) {
                if (t == null || t.text == null || t.text.isBlank()) continue;
                String role = ("model".equalsIgnoreCase(t.role)) ? "model" : "user";
                contents.add(Map.of(
                        "role", role,
                        "parts", List.of(Map.of("text", t.text))
                ));
            }
        }

        contents.add(Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", req.message))
        ));

        Map<String, Object> body = Map.of(
                "contents", contents,
                "generationConfig", Map.of(
                        "temperature", 0.6,
                        "maxOutputTokens", 512
                )
        );

        try {
            String json = om.writeValueAsString(body);

            String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent";
            HttpRequest httpReq = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .header("x-goog-api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> resp = http.send(httpReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (resp.statusCode() / 100 != 2) {
                throw new RuntimeException("GEMINI_HTTP_" + resp.statusCode() + ": " + resp.body());
            }

            Map<?, ?> root = om.readValue(resp.body(), Map.class);
            List<?> candidates = (List<?>) root.get("candidates");
            if (candidates == null || candidates.isEmpty()) return "Mình chưa tạo được câu trả lời, bạn thử lại nhé.";

            Map<?, ?> c0 = (Map<?, ?>) candidates.get(0);
            Map<?, ?> content = (Map<?, ?>) c0.get("content");
            List<?> parts = (List<?>) content.get("parts");
            if (parts == null || parts.isEmpty()) return "Mình chưa tạo được câu trả lời, bạn thử lại nhé.";

            Map<?, ?> p0 = (Map<?, ?>) parts.get(0);
            Object text = p0.get("text");
            return String.valueOf(text == null ? "" : text);

        } catch (Exception e) {
            throw new RuntimeException("GEMINI_CALL_FAILED: " + e.getMessage(), e);
        }
    }
}
