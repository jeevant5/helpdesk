package com.stackroute.helpdesk.exception;

/**
 * Exception thrown when a database persistence or connection error occurs.
 */
public class DatabaseException extends AppException {
    public DatabaseException(String message) {
        super("DB_ERROR", message);
    }

    public DatabaseException(String message, Throwable cause) {
        super("DB_ERROR", message, cause);
    }
}