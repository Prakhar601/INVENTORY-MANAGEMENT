package com.fintrack.util;

import com.fintrack.config.AppConfig;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Currency formatting and parsing utility.
 */
public final class CurrencyUtil {

    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    private CurrencyUtil() {}

    public static String format(double amount) {
        return CURRENCY_FORMAT.format(amount);
    }

    public static String formatSimple(double amount) {
        return AppConfig.DEFAULT_CURRENCY_SYMBOL + String.format("%,.2f", amount);
    }

    public static String toDisplayString(double amount) {
        if (amount >= 0) {
            return "+" + formatSimple(amount);
        }
        return formatSimple(amount);
    }

    public static double parse(String amountString) {
        if (amountString == null || amountString.trim().isEmpty()) return 0.0;
        String cleaned = amountString.replaceAll("[^0-9.\\-]", "");
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
