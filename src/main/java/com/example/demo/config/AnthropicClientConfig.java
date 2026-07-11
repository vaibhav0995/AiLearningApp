package com.example.demo.config;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AnthropicClientConfig {

    @Value("${claude.configuration.api-key}")
    private String apiKey;

    @Value("${claude.configuration.model}")
    private String model;

    @Bean
    public AnthropicClient anthropicClient() {
        System.out.println("Anthropic API Key Resolved: " + apiKey);
        return AnthropicOkHttpClient.builder()
                .apiKey(apiKey)
                .build();
    }

    @Bean(name = "claudeModel")
    public String claudeModel() {
        return model;
    }
}
