package com.fintrack.util;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

public class ToastUtil {

    private static final int TOAST_DELAY_MS = 3000;
    private static final int ANIMATION_TIME_MS = 300;

    public static void showSuccess(String message) {
        showToast(message, "fas-check-circle", "#10B981", "#ECFDF5");
    }

    public static void showError(String message) {
        showToast(message, "fas-exclamation-circle", "#EF4444", "#FEF2F2");
    }

    public static void showInfo(String message) {
        showToast(message, "fas-info-circle", "#3B82F6", "#EFF6FF");
    }

    private static void showToast(String message, String iconLiteral, String iconColor, String bgColor) {
        if (!PreferenceManager.isNotificationsEnabled()) {
            return;
        }
        
        Platform.runLater(() -> {
            Window activeWindow = Window.getWindows().stream().filter(Window::isFocused).findFirst().orElse(null);
            if (activeWindow == null) return;

            Popup popup = new Popup();
            popup.setAutoFix(true);
            popup.setAutoHide(true);
            popup.setHideOnEscape(true);

            HBox toastContent = new HBox(12);
            toastContent.setAlignment(Pos.CENTER_LEFT);
            toastContent.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                "-fx-background-radius: 8px;" +
                "-fx-border-radius: 8px;" +
                "-fx-border-color: " + iconColor + "40;" + // 25% opacity border
                "-fx-border-width: 1px;" +
                "-fx-padding: 12px 20px;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 4);"
            );

            FontIcon icon = new FontIcon(iconLiteral);
            icon.setIconColor(Color.web(iconColor));
            icon.setIconSize(20);

            Label lblMessage = new Label(message);
            lblMessage.setStyle("-fx-text-fill: #1E293B; -fx-font-size: 14px; -fx-font-weight: 500;");

            toastContent.getChildren().addAll(icon, lblMessage);
            popup.getContent().add(toastContent);

            // Positioning
            popup.setOnShown(e -> {
                popup.setX(activeWindow.getX() + activeWindow.getWidth() - toastContent.getWidth() - 30);
                popup.setY(activeWindow.getY() + activeWindow.getHeight() - toastContent.getHeight() - 30);
            });

            // Entry Animation
            toastContent.setOpacity(0);
            toastContent.setTranslateY(20);
            popup.show(activeWindow);

            Timeline fadeIn = new Timeline(
                    new KeyFrame(Duration.millis(ANIMATION_TIME_MS), 
                        new KeyValue(toastContent.opacityProperty(), 1.0),
                        new KeyValue(toastContent.translateYProperty(), 0.0)
                    )
            );

            // Exit Animation
            Timeline fadeOut = new Timeline(
                    new KeyFrame(Duration.millis(ANIMATION_TIME_MS), 
                        new KeyValue(toastContent.opacityProperty(), 0.0),
                        new KeyValue(toastContent.translateYProperty(), 20.0)
                    )
            );
            fadeOut.setOnFinished(e -> popup.hide());

            fadeIn.setOnFinished(e -> {
                Timeline delay = new Timeline(new KeyFrame(Duration.millis(TOAST_DELAY_MS), ev -> fadeOut.play()));
                delay.play();
            });

            fadeIn.play();
        });
    }
}
