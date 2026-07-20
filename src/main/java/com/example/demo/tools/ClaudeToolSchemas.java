package com.example.demo.tools;

import com.anthropic.core.JsonValue;
import com.anthropic.models.messages.Tool;

import java.util.List;
import java.util.Map;

public final class ClaudeToolSchemas {

    private ClaudeToolSchemas() {
    }

    public static List<Tool> all() {
        return List.of(getCurrentDateTimeTool(), addDurationToDateTimeTool(), setReminderTool());
    }

    private static Tool getCurrentDateTimeTool() {
        Tool.InputSchema.Properties properties = Tool.InputSchema.Properties.builder()
                .putAdditionalProperty("outputFormat", JsonValue.from(Map.of(
                        "type", "string",
                        "description", "strftime-style format string for the output, e.g. '%Y-%m-%d %H:%M:%S'. Defaults to '%Y-%m-%d %H:%M:%S'."
                )))
                .build();

        return Tool.builder()
                .name("getCurrentDateTime")
                .description("Gets the current date and time, formatted using the given strftime-style format " +
                        "string. Defaults to '%Y-%m-%d %H:%M:%S' (e.g. '2025-04-03 10:30:00'), which can be fed " +
                        "straight into addDurationToDateTime as its datetimeStr/inputFormat pair.")
                .inputSchema(Tool.InputSchema.builder()
                        .type(JsonValue.from("object"))
                        .properties(properties)
                        .build())
                .build();
    }

    private static Tool addDurationToDateTimeTool() {
        Tool.InputSchema.Properties properties = Tool.InputSchema.Properties.builder()
                .putAdditionalProperty("datetimeStr", JsonValue.from(Map.of(
                        "type", "string",
                        "description", "The input datetime string to which the duration will be added. This should be formatted according to the inputFormat parameter."
                )))
                .putAdditionalProperty("duration", JsonValue.from(Map.of(
                        "type", "number",
                        "description", "The amount of time to add to the datetime. Can be positive (for future dates) or negative (for past dates). Defaults to 0."
                )))
                .putAdditionalProperty("unit", JsonValue.from(Map.of(
                        "type", "string",
                        "description", "The unit of time for the duration. Must be one of: 'seconds', 'minutes', 'hours', 'days', 'weeks', 'months', or 'years'. Defaults to 'days'."
                )))
                .putAdditionalProperty("inputFormat", JsonValue.from(Map.of(
                        "type", "string",
                        "description", "The strftime-style format string for parsing datetimeStr, e.g. '%Y-%m-%d' for dates like '2025-04-03'. Defaults to '%Y-%m-%d'."
                )))
                .build();

        return Tool.builder()
                .name("addDurationToDateTime")
                .description("Adds a specified duration to a datetime string and returns the resulting datetime " +
                        "in a detailed format. Handles seconds, minutes, hours, days, weeks, months, and years, " +
                        "with special handling for month and year calculations to account for varying month " +
                        "lengths and leap years. The output always includes the day of the week, month name, " +
                        "day, year, and time with AM/PM indicator (e.g., 'Thursday, April 03, 2025 10:30:00 AM').")
                .inputSchema(Tool.InputSchema.builder()
                        .type(JsonValue.from("object"))
                        .properties(properties)
                        .addRequired("datetimeStr")
                        .build())
                .build();
    }

    private static Tool setReminderTool() {
        Tool.InputSchema.Properties properties = Tool.InputSchema.Properties.builder()
                .putAdditionalProperty("content", JsonValue.from(Map.of(
                        "type", "string",
                        "description", "The message text that will be displayed in the reminder notification, e.g. 'Take medication' or 'Join video call with team'."
                )))
                .putAdditionalProperty("timestamp", JsonValue.from(Map.of(
                        "type", "string",
                        "description", "The exact date and time when the reminder should be triggered, formatted as an ISO 8601 timestamp (YYYY-MM-DDTHH:MM:SS)."
                )))
                .build();

        return Tool.builder()
                .name("setReminder")
                .description("Creates a timed reminder that will notify the user at the specified time with the " +
                        "provided content. Use this when a user wants to be reminded about something specific at " +
                        "a future point in time, such as meetings, tasks, medication schedules, or other " +
                        "time-bound activities.")
                .inputSchema(Tool.InputSchema.builder()
                        .type(JsonValue.from("object"))
                        .properties(properties)
                        .addRequired("content")
                        .addRequired("timestamp")
                        .build())
                .build();
    }
}
