package com.stackroute.helpdesk.exception;

/**
 * Exception thrown when a requested resource (ticket, user, comment) cannot be found.
 */
public class ResourceNotFoundException extends AppException {
    public ResourceNotFoundException(String resourceName, Object identifier) {
        super("NOT_FOUND", resourceName + " with identifier '" + identifier + "' was not found.");
    }
}