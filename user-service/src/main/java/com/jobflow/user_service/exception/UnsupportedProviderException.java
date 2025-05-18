package com.jobflow.user_service.exception;

public class UnsupportedProviderException extends RuntimeException {
    public UnsupportedProviderException(String message) {
        super(message);
    }
}
