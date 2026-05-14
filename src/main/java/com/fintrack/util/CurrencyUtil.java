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
        String currencyCode = PreferenceManager.getCurrency();
        String symbol = "$"; // Default USD
        switch (currencyCode) {
            case "EUR": symbol = "€"; break;
            case "GBP": symbol = "£"; break;
            case "INR": symbol = "₹"; break;
            case "JPY": symbol = "¥"; break;
            case "CAD": symbol = "C$"; break;
            case "AUD": symbol = "A$"; break;
        }
        return symbol + String.format("%,.2f", amount);
    }

    /**
     * Returns the currency symbol for the user's selected currency.
     * Safe to use in controller code to set Label text at runtime.
     */
    public static String getSymbol() {
        String currencyCode = PreferenceManager.getCurrency();
        return switch (currencyCode) {
            case "EUR" -> "\u20AC";
            case "GBP" -> "\u00A3";
            case "INR" -> "\u20B9";
            case "JPY" -> "\u00A5";
            case "CAD" -> "C$";
            case "AUD" -> "A$";
            default -> "$";
        };
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
