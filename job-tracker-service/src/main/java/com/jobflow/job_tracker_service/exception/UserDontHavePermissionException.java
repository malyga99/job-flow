package com.jobflow.job_tracker_service.exception;

public class UserDontHavePermissionException extends RuntimeException {
    public UserDontHavePermissionException(String message) {
        super(message);
    }
}
