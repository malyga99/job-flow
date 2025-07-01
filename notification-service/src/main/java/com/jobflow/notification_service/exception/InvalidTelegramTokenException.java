package com.jobflow.notification_service.exception;

public class InvalidTelegramTokenException extends RuntimeException {
    public InvalidTelegramTokenException(String message) {
        super(message);
    }
}
