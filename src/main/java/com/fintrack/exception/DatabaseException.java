package com.fintrack.exception;

/**
 * Exception thrown when a database operation fails.
 * <p>
 * DAO implementations catch {@link java.sql.SQLException} and wrap
 * them in this exception to decouple upper layers from JDBC details.
 * </p>
 */
public class DatabaseException extends AppException {

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
