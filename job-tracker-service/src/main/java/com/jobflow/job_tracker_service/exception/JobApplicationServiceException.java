package com.jobflow.job_tracker_service.exception;

public class JobApplicationServiceException extends RuntimeException {
    public JobApplicationServiceException(String message) {
        super(message);
    }

    public JobApplicationServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
