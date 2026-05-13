package com.fintrack;

/**
 * Non-JavaFX main class entry point.
 * <p>
 * This exists as a workaround for the JavaFX module system requirement.
 * The {@code javafx-maven-plugin} expects a class that does NOT extend
 * {@link javafx.application.Application} as the main class when the
 * module system is in play. This launcher simply delegates to {@link App}.
 * </p>
 */
public class Launcher {

    public static void main(String[] args) {
        App.main(args);
    }
}
