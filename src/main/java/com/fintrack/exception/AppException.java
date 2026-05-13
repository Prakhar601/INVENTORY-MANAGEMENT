package com.fintrack.exception;

/**
 * Base unchecked exception for all FinTrack application errors.
 * <p>
 * All custom exceptions in the application extend this class,
 * providing a unified exception hierarchy for consistent error handling.
 * </p>
 */
public class AppException extends RuntimeException {

    public AppException(String message) {
        super(message);
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
    }
}
