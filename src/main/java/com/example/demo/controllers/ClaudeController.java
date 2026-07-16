package com.example.demo.controllers;

import com.example.demo.services.ClaudeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/claude")
public class ClaudeController {

    private final ClaudeService claudeService;

    @Autowired
    public ClaudeController(ClaudeService claudeService) {
        this.claudeService = claudeService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        String reply = claudeService.chat(request.message());
        return ResponseEntity.ok(new ChatResponse(reply));
    }

    @PostMapping("/system-prompt")
    public ResponseEntity<Void> systemPrompt(@RequestBody SystemPromptRequest request) {
        claudeService.setSystemPrompt(request.systemPrompt());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset")
    public ResponseEntity<Void> reset() {
        claudeService.resetChat();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/sentiment")
    public ResponseEntity<SentimentResponse> sentiment(@RequestBody TweetRequest request) {
        String sentiment = claudeService.analyzeSentiment(request.tweet());
        return ResponseEntity.ok(new SentimentResponse(sentiment));
    }

    public record ChatRequest(String message) {}
    public record ChatResponse(String reply) {}
    public record SystemPromptRequest(String systemPrompt) {}
    public record TweetRequest(String tweet) {}
    public record SentimentResponse(String sentiment) {}
}
