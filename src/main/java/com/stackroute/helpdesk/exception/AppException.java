package com.stackroute.helpdesk.exception;

/**
 * Base unchecked exception for all application runtime errors.
 */
public class AppException extends RuntimeException {
    private final String errorCode;

    public AppException(String message) {
        super(message);
        this.errorCode = "APP_ERROR";
    }

    public AppException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "APP_ERROR";
    }

    public AppException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}