package com.fintrack.util;

import java.util.prefs.Preferences;

/**
 * Manages user preferences persistently using Java's Preferences API.
 */
public class PreferenceManager {

    private static final Preferences prefs = Preferences.userNodeForPackage(PreferenceManager.class);

    private static final String KEY_THEME = "app_theme";
    private static final String KEY_CURRENCY = "app_currency";
    private static final String KEY_NOTIFICATIONS = "app_notifications";

    public static void setTheme(String theme) {
        prefs.put(KEY_THEME, theme);
    }

    public static String getTheme() {
        return prefs.get(KEY_THEME, "Light"); // Default to Light
    }

    public static void setCurrency(String currency) {
        prefs.put(KEY_CURRENCY, currency);
    }

    public static String getCurrency() {
        return prefs.get(KEY_CURRENCY, "USD");
    }

    public static void setNotificationsEnabled(boolean enabled) {
        prefs.putBoolean(KEY_NOTIFICATIONS, enabled);
    }

    public static boolean isNotificationsEnabled() {
        return prefs.getBoolean(KEY_NOTIFICATIONS, true);
    }
}
