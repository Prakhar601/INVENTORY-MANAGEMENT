package com.fintrack.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Handles presenting detailed, exact runtime exceptions to the end user.
 */
public class ErrorDialogUtil {

    /**
     * Translates backend errors into exact UI popups with full stack traces.
     */
    public static void showFriendlyError(Throwable t) {
        // 1. ALWAYS print full stack trace to console first
        t.printStackTrace();

        Platform.runLater(() -> {
            // Validation errors are still user-facing so we keep them clean
            if (t instanceof com.fintrack.exception.ValidationException) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Validation Error");
                alert.setHeaderText("Invalid Input");
                alert.setContentText(t.getMessage());
                alert.showAndWait();
                return;
            }

            // Expose REAL exception details
            String exceptionType = t.getClass().getName();
            String rootCause = getRootCause(t).getClass().getName();
            String exactMessage = t.getMessage() != null ? t.getMessage() : "No message provided.";
            
            String failingLine = "Unknown Location";
            if (t.getStackTrace().length > 0) {
                StackTraceElement element = t.getStackTrace()[0];
                failingLine = element.getClassName() + ":" + element.getLineNumber();
            }

            String title = "Runtime Fatal Exception";
            String header = "Exception: " + exceptionType;
            String content = "Root Cause: " + rootCause + "\n"
                           + "Failing Line: " + failingLine + "\n"
                           + "Message: " + exactMessage;

            showDetailedCrashDialog(title, header, content, t);
        });
    }

    private static Throwable getRootCause(Throwable t) {
        Throwable rootCause = t;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }
        return rootCause;
    }

    /**
     * Shows a severe error dialog with an expandable "Show Details" area containing the stack trace.
     */
    private static void showDetailedCrashDialog(String title, String header, String content, Throwable ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        // Create expandable Exception.
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("Full Stack Trace:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        alert.getDialogPane().setExpandableContent(expContent);

        try {
            alert.showAndWait();
        } catch (Exception e) {
            // Failsafe if JavaFX toolkit is completely dead
            System.err.println("CRITICAL UI CRASH: Could not render ErrorDialog.");
            e.printStackTrace();
        }
    }
}
