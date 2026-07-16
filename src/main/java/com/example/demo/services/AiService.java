package com.example.demo.services;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AiService {

    private static final double TEMPERATURE = 0.7;

    private static final String SENTIMENT_SYSTEM_PROMPT = """
            You are a sentiment classification engine for tweets. Classify the sentiment \
            of the tweet the user gives you as exactly one word: POSITIVE or NEGATIVE. \
            Respond with only that single word in uppercase and nothing else - no \
            punctuation, no explanation.""";

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

        ChatOptions chatOptions = ChatOptions.builder()
                .temperature(TEMPERATURE)
                .build();

        String reply = chatClient.prompt(new Prompt(messages, chatOptions))
                .call()
                .content();

        conversationHistory.add(new AssistantMessage(reply));
        return reply;
    }

    public void resetChat() {
        conversationHistory.clear();
        systemPrompt = null;
    }

    public String analyzeSentiment(String tweet) {
        List<Message> messages = List.of(
                new SystemMessage(SENTIMENT_SYSTEM_PROMPT),
                new UserMessage(tweet)
        );

        ChatOptions chatOptions = ChatOptions.builder()
                .temperature(TEMPERATURE)
                .build();

        String reply = chatClient.prompt(new Prompt(messages, chatOptions))
                .call()
                .content();

        return reply == null ? "" : reply.trim();
    }
}
