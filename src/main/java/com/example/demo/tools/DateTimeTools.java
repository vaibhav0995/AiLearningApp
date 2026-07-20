package com.example.demo.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Locale;

@Component
public class DateTimeTools {

    private static final String DEFAULT_DATETIME_FORMAT = "%Y-%m-%d %H:%M:%S";
    private static final DateTimeFormatter DETAILED_OUTPUT_FORMATTER =
            DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy hh:mm:ss a", Locale.ENGLISH);

    @Tool(description = "Gets the current date and time, formatted using the given strftime-style format string. " +
            "Defaults to '%Y-%m-%d %H:%M:%S' (e.g. '2025-04-03 10:30:00'), which can be fed straight into " +
            "addDurationToDateTime as its datetimeStr/inputFormat pair.")
    public String getCurrentDateTime(
            @ToolParam(required = false, description = "strftime-style format string for the output, e.g. '%Y-%m-%d %H:%M:%S'. Defaults to '%Y-%m-%d %H:%M:%S'.")
            String outputFormat) {
        String format = isBlank(outputFormat) ? DEFAULT_DATETIME_FORMAT : outputFormat;
        return LocalDateTime.now().format(toJavaFormatter(format));
    }

    @Tool(description = "Adds a specified duration to a datetime string and returns the resulting datetime in a " +
            "detailed format. This tool converts an input datetime string to a datetime value, adds the specified " +
            "duration in the requested unit, and returns a formatted string of the resulting datetime. It handles " +
            "seconds, minutes, hours, days, weeks, months, and years, with special handling for month and year " +
            "calculations to account for varying month lengths and leap years. The output is always returned in a " +
            "detailed format that includes the day of the week, month name, day, year, and time with AM/PM " +
            "indicator (e.g., 'Thursday, April 03, 2025 10:30:00 AM').")
    public String addDurationToDateTime(
            @ToolParam(description = "The input datetime string to which the duration will be added. This should be formatted according to the inputFormat parameter.")
            String datetimeStr,
            @ToolParam(required = false, description = "The amount of time to add to the datetime. Can be positive (for future dates) or negative (for past dates). Defaults to 0.")
            Double duration,
            @ToolParam(required = false, description = "The unit of time for the duration. Must be one of: 'seconds', 'minutes', 'hours', 'days', 'weeks', 'months', or 'years'. Defaults to 'days'.")
            String unit,
            @ToolParam(required = false, description = "The strftime-style format string for parsing datetimeStr, e.g. '%Y-%m-%d' for dates like '2025-04-03'. Defaults to '%Y-%m-%d'.")
            String inputFormat) {

        long amount = duration == null ? 0 : duration.longValue();
        String resolvedUnit = isBlank(unit) ? "days" : unit;
        String resolvedFormat = isBlank(inputFormat) ? "%Y-%m-%d" : inputFormat;

        LocalDateTime date = LocalDateTime.parse(datetimeStr, toJavaFormatter(resolvedFormat));

        LocalDateTime newDate = switch (resolvedUnit) {
            case "seconds" -> date.plusSeconds(amount);
            case "minutes" -> date.plusMinutes(amount);
            case "hours" -> date.plusHours(amount);
            case "days" -> date.plusDays(amount);
            case "weeks" -> date.plusWeeks(amount);
            case "months" -> date.plusMonths(amount);
            case "years" -> date.plusYears(amount);
            default -> throw new IllegalArgumentException("Unsupported time unit: " + resolvedUnit);
        };

        return newDate.format(DETAILED_OUTPUT_FORMATTER);
    }

    @Tool(description = "Creates a timed reminder that will notify the user at the specified time with the " +
            "provided content. Use this when a user wants to be reminded about something specific at a future " +
            "point in time, such as meetings, tasks, medication schedules, or other time-bound activities.")
    public String setReminder(
            @ToolParam(description = "The message text that will be displayed in the reminder notification, e.g. 'Take medication' or 'Join video call with team'.")
            String content,
            @ToolParam(description = "The exact date and time when the reminder should be triggered, formatted as an ISO 8601 timestamp (YYYY-MM-DDTHH:MM:SS).")
            String timestamp) {
        System.out.println("----\nSetting the following reminder for " + timestamp + ":\n" + content + "\n----");
        return "Reminder set for " + timestamp + ": " + content;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static DateTimeFormatter toJavaFormatter(String pythonFormat) {
        return new DateTimeFormatterBuilder()
                .appendPattern(translatePythonFormat(pythonFormat))
                .parseDefaulting(ChronoField.MONTH_OF_YEAR, 1)
                .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                .toFormatter(Locale.ENGLISH);
    }

    private static String translatePythonFormat(String pythonFormat) {
        StringBuilder pattern = new StringBuilder();
        int i = 0;
        while (i < pythonFormat.length()) {
            char c = pythonFormat.charAt(i);
            if (c == '%' && i + 1 < pythonFormat.length()) {
                pattern.append(mapDirective(pythonFormat.charAt(i + 1)));
                i += 2;
            } else if (Character.isLetter(c)) {
                pattern.append('\'').append(c).append('\'');
                i++;
            } else {
                pattern.append(c);
                i++;
            }
        }
        return pattern.toString();
    }

    private static String mapDirective(char directive) {
        return switch (directive) {
            case 'Y' -> "yyyy";
            case 'y' -> "yy";
            case 'm' -> "MM";
            case 'd' -> "dd";
            case 'H' -> "HH";
            case 'M' -> "mm";
            case 'S' -> "ss";
            case 'I' -> "hh";
            case 'p' -> "a";
            case 'B' -> "MMMM";
            case 'b' -> "MMM";
            case 'A' -> "EEEE";
            case 'a' -> "EEE";
            case 'j' -> "DDD";
            case 'z' -> "Z";
            default -> throw new IllegalArgumentException("Unsupported datetime format directive: %" + directive);
        };
    }
}
