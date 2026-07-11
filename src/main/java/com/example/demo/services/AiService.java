package com.example.demo.services;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AiService {

    private final ChatClient chatClient;
    private final List<Message> conversationHistory = new ArrayList<>();
    private String systemPrompt;

    public AiService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public String chat(String userMessage) {
        conversationHistory.add(new UserMessage(userMessage));

        List<Message> messages = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            messages.add(new SystemMessage(systemPrompt));
        }
        messages.addAll(conversationHistory);

        String reply = chatClient.prompt(new Prompt(messages))
                .call()
                .content();

        conversationHistory.add(new AssistantMessage(reply));
        return reply;
    }

    public void resetChat() {
        conversationHistory.clear();
        systemPrompt = null;
    }
}
