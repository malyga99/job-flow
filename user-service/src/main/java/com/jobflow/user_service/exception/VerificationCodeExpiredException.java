package com.jobflow.user_service.exception;

public class VerificationCodeExpiredException extends RuntimeException {
  public VerificationCodeExpiredException(String message) {
    super(message);
  }
}
