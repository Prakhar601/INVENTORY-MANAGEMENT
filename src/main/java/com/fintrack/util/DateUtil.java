package com.fintrack.util;

import com.fintrack.config.AppConfig;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Date formatting and parsing utility.
 */
public final class DateUtil {

    private static final DateTimeFormatter DEFAULT_FORMATTER =
            DateTimeFormatter.ofPattern(AppConfig.DEFAULT_DATE_FORMAT);
    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern(AppConfig.DISPLAY_DATE_FORMAT);

    private DateUtil() {}

    public static String format(LocalDate date) {
        return date != null ? date.format(DEFAULT_FORMATTER) : "";
    }

    public static String formatForDisplay(LocalDate date) {
        return date != null ? date.format(DISPLAY_FORMATTER) : "";
    }

    public static LocalDate parse(String dateString) {
        try {
            return LocalDate.parse(dateString, DEFAULT_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static String toSqlDate(LocalDate date) {
        return date != null ? date.toString() : null;
    }

    public static boolean isInRange(LocalDate date, LocalDate from, LocalDate to) {
        if (date == null || from == null || to == null) return false;
        return !date.isBefore(from) && !date.isAfter(to);
    }

    public static LocalDate getFirstDayOfMonth() {
        return LocalDate.now().withDayOfMonth(1);
    }

    public static LocalDate getLastDayOfMonth() {
        LocalDate now = LocalDate.now();
        return now.withDayOfMonth(now.lengthOfMonth());
    }
}
