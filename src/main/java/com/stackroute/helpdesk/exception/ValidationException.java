package com.stackroute.helpdesk.exception;

/**
 * Exception thrown when user input or business rules validation fails.
 */
public class ValidationException extends AppException {
    public ValidationException(String message) {
        super("VALIDATION_ERROR", message);
    }
}