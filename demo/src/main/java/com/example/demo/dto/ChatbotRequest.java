package com.example.demo.dto;

import java.util.List;

public class ChatbotRequest {
	public String message;
    public List<ChatbotTurn> history;

    public static class ChatbotTurn {
        public String role;
        public String text;
    }

}
