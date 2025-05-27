package com.jobflow.job_tracker_service.exception;

public class JobApplicationNotFoundException extends RuntimeException {
    public JobApplicationNotFoundException(String message) {
        super(message);
    }
}
