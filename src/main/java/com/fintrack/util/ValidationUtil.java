package com.fintrack.util;

import com.fintrack.exception.ValidationException;

/**
 * Input validation utility with fail-fast semantics.
 * <p>
 * Methods throw {@link ValidationException} on failure, providing
 * user-friendly messages that controllers can display directly.
 * </p>
 */
public final class ValidationUtil {

    private ValidationUtil() {}

    public static void requireNonNull(Object value, String fieldName) {
        if (value == null) {
            throw new ValidationException(fieldName + " is required.");
        }
    }

    public static void requireNotBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " cannot be blank.");
        }
    }

    public static void requirePositive(double value, String fieldName) {
        if (value <= 0) {
            throw new ValidationException(fieldName + " must be a positive number.");
        }
    }

    public static void requireMinLength(String value, String fieldName, int minLength) {
        if (value == null || value.length() < minLength) {
            throw new ValidationException(fieldName + " must be at least " + minLength + " characters.");
        }
    }

    public static void requireMaxLength(String value, String fieldName, int maxLength) {
        if (value != null && value.length() > maxLength) {
            throw new ValidationException(fieldName + " must be at most " + maxLength + " characters.");
        }
    }

    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    public static void requireValidEmail(String email) {
        if (!isValidEmail(email)) {
            throw new ValidationException("Please enter a valid email address.");
        }
    }

    public static void requirePasswordsMatch(String password, String confirmPassword) {
        if (password == null || !password.equals(confirmPassword)) {
            throw new ValidationException("Passwords do not match.");
        }
    }

    public static boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
