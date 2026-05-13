package com.fintrack.exception;

/**
 * Exception thrown when authentication or authorization fails.
 * <p>
 * Used by {@link com.fintrack.service.AuthService} for invalid
 * credentials, locked accounts, or session expiration.
 * </p>
 */
public class AuthenticationException extends AppException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
