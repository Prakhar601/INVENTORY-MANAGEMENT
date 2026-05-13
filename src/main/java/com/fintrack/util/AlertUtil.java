package com.fintrack.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 * Standardized JavaFX alert dialogs.
 * <p>
 * Provides static methods for success, warning, error, and
 * confirmation dialogs with consistent styling.
 * </p>
 */
public final class AlertUtil {

    private AlertUtil() {}

    public static void showSuccess(String message) {
        showAlert(Alert.AlertType.INFORMATION, "Success", message);
    }

    public static void showWarning(String message) {
        showAlert(Alert.AlertType.WARNING, "Warning", message);
    }

    public static void showError(String message) {
        showAlert(Alert.AlertType.ERROR, "Error", message);
    }

    public static void showInfo(String message) {
        showAlert(Alert.AlertType.INFORMATION, "Information", message);
    }

    /**
     * Shows a confirmation dialog and returns {@code true} if the user clicks OK.
     */
    public static boolean showConfirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
