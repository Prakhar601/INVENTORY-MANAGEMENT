package com.fintrack.exception;

/**
 * Exception thrown when input validation fails.
 * <p>
 * Used by the service layer to reject invalid data before
 * it reaches the database. Controllers catch this to display
 * user-friendly validation messages.
 * </p>
 */
public class ValidationException extends AppException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
