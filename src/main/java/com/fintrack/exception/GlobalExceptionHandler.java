package com.fintrack.exception;

import com.fintrack.util.ErrorDialogUtil;
import com.fintrack.util.LoggerUtil;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Global uncaught exception handler for the entire JavaFX Application.
 * Prevents the application from silently crashing and ensures
 * all panics are logged and presented to the user.
 */
public class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final Logger LOGGER = LoggerUtil.getLogger(GlobalExceptionHandler.class);

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        // 1. Log the absolute fatal crash
        LOGGER.log(Level.SEVERE, "UNCAUGHT EXCEPTION in thread " + t.getName() + ": " + e.getMessage(), e);

        // 2. Safely translate and show the error to the UI without crashing the event loop
        ErrorDialogUtil.showFriendlyError(e);
    }
}
