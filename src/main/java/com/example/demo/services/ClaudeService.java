package com.example.demo.services;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StopReason;
import com.anthropic.models.messages.Tool;
import com.anthropic.models.messages.ToolResultBlockParam;
import com.anthropic.models.messages.ToolUseBlock;
import com.anthropic.models.messages.WebSearchTool20250305;
import com.example.demo.tools.ClaudeToolSchemas;
import com.example.demo.tools.DateTimeTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ClaudeService {

    private static final String SENTIMENT_SYSTEM_PROMPT = """
            You are a sentiment classification engine for tweets. Classify the sentiment \
            of the tweet the user gives you as exactly one word: POSITIVE or NEGATIVE. \
            Respond with only that single word in uppercase and nothing else - no \
            punctuation, no explanation.""";

    private static final long WEB_SEARCH_MAX_USES = 5L;

    private final AnthropicClient anthropicClient;
    private final String claudeModel;
    private final DateTimeTools dateTimeTools;
    private final List<Tool> tools;
    private final WebSearchTool20250305 webSearchTool;

    private record ConversationMessage(String role, String content) {}
    private final List<ConversationMessage> conversationHistory = new ArrayList<>();
    private String systemPrompt;

    @Autowired
    public ClaudeService(AnthropicClient anthropicClient, @Qualifier("claudeModel") String claudeModel, DateTimeTools dateTimeTools) {
        this.anthropicClient = anthropicClient;
        this.claudeModel = claudeModel;
        this.dateTimeTools = dateTimeTools;
        this.tools = ClaudeToolSchemas.all();
        this.webSearchTool = WebSearchTool20250305.builder()
                .maxUses(WEB_SEARCH_MAX_USES)
                .build();
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

        tools.forEach(builder::addTool);
        builder.addTool(webSearchTool);

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

        String reply = runToolLoop(builder);

        if (!reply.isEmpty()) {
            conversationHistory.add(new ConversationMessage("assistant", reply));
        }

        return reply;
    }

    private String runToolLoop(MessageCreateParams.Builder builder) {
        while (true) {
            Message response = anthropicClient.messages().create(builder.build());

            if (response.stopReason().isEmpty() || !StopReason.TOOL_USE.equals(response.stopReason().get())) {
                return response.content().stream()
                        .flatMap(block -> block.text().stream())
                        .map(textBlock -> textBlock.text())
                        .findFirst()
                        .orElse("");
            }

            builder.addMessage(response);

            List<ContentBlockParam> toolResults = new ArrayList<>();
            for (ContentBlock block : response.content()) {
                if (block.isToolUse()) {
                    ToolUseBlock toolUse = block.asToolUse();
                    toolResults.add(ContentBlockParam.ofToolResult(
                            ToolResultBlockParam.builder()
                                    .toolUseId(toolUse.id())
                                    .content(executeTool(toolUse))
                                    .build()));
                }
            }
            builder.addUserMessageOfBlockParams(toolResults);
        }
    }

    @SuppressWarnings("unchecked")
    private String executeTool(ToolUseBlock toolUse) {
        Map<String, Object> input = toolUse._input().convert(Map.class);
        try {
            return switch (toolUse.name()) {
                case "getCurrentDateTime" -> dateTimeTools.getCurrentDateTime((String) input.get("outputFormat"));
                case "addDurationToDateTime" -> dateTimeTools.addDurationToDateTime(
                        (String) input.get("datetimeStr"),
                        toDouble(input.get("duration")),
                        (String) input.get("unit"),
                        (String) input.get("inputFormat"));
                case "setReminder" -> dateTimeTools.setReminder(
                        (String) input.get("content"),
                        (String) input.get("timestamp"));
                default -> "Unknown tool: " + toolUse.name();
            };
        } catch (Exception e) {
            return "Error executing tool " + toolUse.name() + ": " + e.getMessage();
        }
    }

    private static Double toDouble(Object value) {
        return value == null ? null : ((Number) value).doubleValue();
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
