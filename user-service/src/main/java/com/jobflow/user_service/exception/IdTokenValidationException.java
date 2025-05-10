package com.jobflow.user_service.exception;

public class IdTokenValidationException extends RuntimeException {
    public IdTokenValidationException(String message) {
        super(message);
    }
}
