package com.example.demo.controller;

import com.example.demo.dto.ChatbotDtos.AskReq;
import com.example.demo.dto.ChatbotDtos.AskRes;
import com.example.demo.service.ChatbotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

  private final ChatbotService chatbot;

  public ChatbotController(ChatbotService chatbot) {
    this.chatbot = chatbot;
  }

  @PostMapping("/ask")
  public ResponseEntity<?> ask(@RequestBody AskReq req) {
    if (req == null || req.message == null || req.message.trim().isEmpty()) {
      return ResponseEntity.badRequest().body(Map.of("message", "EMPTY_MESSAGE"));
    }
    var r = chatbot.askPublicWithProducts(req.message);
    return ResponseEntity.ok(new AskRes(r.reply, r.products));
  }
}
