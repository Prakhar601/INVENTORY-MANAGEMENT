package com.fintrack.util;

import com.fintrack.config.AppConfig;
import javafx.scene.Scene;
import javafx.stage.Window;

import java.net.URL;

/**
 * Manages the application-wide theme (Light/Dark).
 */
public class ThemeManager {

    public static void applyTheme(Scene scene) {
        if (scene == null) return;
        
        String theme = PreferenceManager.getTheme();
        URL darkCss = ThemeManager.class.getResource(AppConfig.CSS_BASE_PATH + "dark-theme.css");
        
        if (darkCss != null) {
            String cssPath = darkCss.toExternalForm();
            if ("Dark".equalsIgnoreCase(theme)) {
                if (!scene.getStylesheets().contains(cssPath)) {
                    scene.getStylesheets().add(cssPath);
                }
            } else {
                scene.getStylesheets().remove(cssPath);
            }
        } else {
            System.err.println("[WARN-THEME] dark-theme.css not found.");
        }
    }
    
    /**
     * Applies the current theme to all open windows.
     */
    public static void refreshAllThemes() {
        for (Window window : Window.getWindows()) {
            if (window.getScene() != null) {
                applyTheme(window.getScene());
            }
        }
    }
}
