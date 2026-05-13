package com.fintrack.config;

/**
 * Centralized application configuration constants.
 * <p>
 * Holds application-wide settings such as the app name, version,
 * default window dimensions, and formatting defaults.
 * </p>
 */
public final class AppConfig {

    private AppConfig() {
        // Prevent instantiation
    }

    // ── Application Metadata ───────────────────────────────────────────
    public static final String APP_NAME    = "FinTrack";
    public static final String APP_VERSION = "1.0.0";
    public static final String APP_TITLE   = APP_NAME + " — Personal Finance Manager";

    // ── Window Defaults ────────────────────────────────────────────────
    public static final double DEFAULT_WIDTH  = 1200;
    public static final double DEFAULT_HEIGHT = 800;
    public static final double MIN_WIDTH      = 1000;
    public static final double MIN_HEIGHT     = 700;

    // ── Formatting Defaults ────────────────────────────────────────────
    public static final String DEFAULT_DATE_FORMAT     = "yyyy-MM-dd";
    public static final String DISPLAY_DATE_FORMAT     = "MMM dd, yyyy";
    public static final String DEFAULT_CURRENCY_SYMBOL = "₹";
    public static final String DEFAULT_CURRENCY_CODE   = "INR";

    // ── Resource Paths ─────────────────────────────────────────────────
    public static final String FXML_BASE_PATH = "/com/fintrack/fxml/";
    public static final String CSS_BASE_PATH  = "/com/fintrack/css/";
    public static final String IMG_BASE_PATH  = "/images/";
}
