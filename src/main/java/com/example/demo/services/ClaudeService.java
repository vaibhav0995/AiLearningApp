package com.example.demo.services;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ClaudeService {

    private static final String SENTIMENT_SYSTEM_PROMPT = """
            You are a sentiment classification engine for tweets. Classify the sentiment \
            of the tweet the user gives you as exactly one word: POSITIVE or NEGATIVE. \
            Respond with only that single word in uppercase and nothing else - no \
            punctuation, no explanation.""";

    private final AnthropicClient anthropicClient;
    private final String claudeModel;

    private record ConversationMessage(String role, String content) {}
    private final List<ConversationMessage> conversationHistory = new ArrayList<>();
    private String systemPrompt;

    @Autowired
    public ClaudeService(AnthropicClient anthropicClient, @Qualifier("claudeModel") String claudeModel) {
        this.anthropicClient = anthropicClient;
        this.claudeModel = claudeModel;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public String chat(String userMessage) {
        conversationHistory.add(new ConversationMessage("user", userMessage));

        MessageCreateParams.Builder builder = MessageCreateParams.builder()
                .model(claudeModel)
                .maxTokens(1000L)
                .temperature(1.0);

        if (systemPrompt != null && !systemPrompt.isBlank()) {
            builder.system(systemPrompt);
        }

        for (ConversationMessage msg : conversationHistory) {
            if ("user".equals(msg.role())) {
                builder.addUserMessage(msg.content());
            } else {
                builder.addAssistantMessage(msg.content());
            }
        }

        Message response = anthropicClient.messages().create(builder.build());

        String reply = response.content().stream()
                .flatMap(block -> block.text().stream())
                .map(textBlock -> textBlock.text())
                .findFirst()
                .orElse("");

        if (!reply.isEmpty()) {
            conversationHistory.add(new ConversationMessage("assistant", reply));
        }

        return reply;
    }

    public void resetChat() {
        conversationHistory.clear();
        systemPrompt = null;
    }

    public String analyzeSentiment(String tweet) {
        MessageCreateParams params = MessageCreateParams.builder()
                .model(claudeModel)
                .maxTokens(1000L)
                .system(SENTIMENT_SYSTEM_PROMPT)
                .addUserMessage(tweet)
                .build();

        Message response = anthropicClient.messages().create(params);

        return response.content().stream()
                .flatMap(block -> block.text().stream())
                .map(textBlock -> textBlock.text().trim())
                .findFirst()
                .orElse("");
    }
}
