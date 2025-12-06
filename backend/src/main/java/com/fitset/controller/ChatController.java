package com.fitset.controller;

import com.fitset.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatController {
    
    private final GeminiService geminiService;
    
    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody Map<String, String> request) {
        try {
            String userMessage = request.get("message");
            if (userMessage == null || userMessage.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Message is required"));
            }
            
            String response = geminiService.generateResponse(userMessage);
            return ResponseEntity.ok(Map.of("response", response));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to generate response: " + e.getMessage()));
        }
    }
    
    @PostMapping("/fitness-chat")
    public ResponseEntity<?> fitnessChat(@RequestBody Map<String, Object> request) {
        try {
            String userMessage = (String) request.get("message");
            String conversationHistory = (String) request.getOrDefault("history", "");
            
            if (userMessage == null || userMessage.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Message is required"));
            }
            
            String response = geminiService.generateFitnessResponse(userMessage, conversationHistory);
            return ResponseEntity.ok(Map.of("response", response));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to generate response: " + e.getMessage()));
        }
    }
}
