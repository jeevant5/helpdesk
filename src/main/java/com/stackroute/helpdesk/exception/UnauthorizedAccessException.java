package com.stackroute.helpdesk.exception;

/**
 * Exception thrown when an authenticated user attempts to access a restricted resource.
 */
public class UnauthorizedAccessException extends AppException {
    public UnauthorizedAccessException(String message) {
        super("UNAUTHORIZED", message);
    }
}