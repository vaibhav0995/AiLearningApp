package com.example.demo.controllers;

import com.example.demo.services.AiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        String reply = aiService.chat(request.message());
        return ResponseEntity.ok(new ChatResponse(reply));
    }

    @PostMapping("/system-prompt")
    public ResponseEntity<Void> systemPrompt(@RequestBody SystemPromptRequest request) {
        aiService.setSystemPrompt(request.systemPrompt());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset")
    public ResponseEntity<Void> reset() {
        aiService.resetChat();
        return ResponseEntity.noContent().build();
    }

    public record ChatRequest(String message) {}
    public record ChatResponse(String reply) {}
    public record SystemPromptRequest(String systemPrompt) {}
}
