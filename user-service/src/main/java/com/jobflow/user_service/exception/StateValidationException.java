package com.jobflow.user_service.exception;

public class StateValidationException extends RuntimeException {
    public StateValidationException(String message) {
        super(message);
    }
}
