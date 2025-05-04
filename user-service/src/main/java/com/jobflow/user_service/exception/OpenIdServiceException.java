package com.jobflow.user_service.exception;

public class OpenIdServiceException extends RuntimeException {
    public OpenIdServiceException(String message) {
        super(message);
    }

    public OpenIdServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
