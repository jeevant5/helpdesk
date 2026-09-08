package com.stackroute.helpdesk.exception;

/**
 * Exception thrown when authentication fails.
 */
public class AuthenticationException extends AppException {
    public AuthenticationException(String message) {
        super("AUTH_ERROR", message);
    }
}