package com.jobflow.user_service.exception;

public class EmailServiceException extends RuntimeException{
    public EmailServiceException(String message) {
        super(message);
    }

    public EmailServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
