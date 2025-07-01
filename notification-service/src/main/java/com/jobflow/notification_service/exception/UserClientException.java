package com.jobflow.notification_service.exception;

public class UserClientException extends RuntimeException {
    public UserClientException(String message) {
        super(message);
    }

    public UserClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
