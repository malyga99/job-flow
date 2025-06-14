package com.jobflow.job_tracker_service.jobApplication;

import com.jobflow.job_tracker_service.rateLimiter.RateLimiterAction;

import java.time.Duration;

public enum JobApplicationRateLimiterAction implements RateLimiterAction {

    CREATE("create", 5, Duration.ofMinutes(1), "Too many job applications created. Try again in a minute"),
    UPDATE("update", 10, Duration.ofMinutes(1), "Too many updates. Try again in a minute"),
    UPDATE_STATUS("updateStatus", 10, Duration.ofMinutes(1), "Too many status updates. Try again in a minute"),
    DELETE("delete", 3, Duration.ofMinutes(1), "Too many delete attempts. Try again in a minute");

    private final String action;
    private final int limit;
    private final Duration timeout;
    private final String message;

    JobApplicationRateLimiterAction(String action, int limit, Duration timeout, String message) {
        this.action = action;
        this.limit = limit;
        this.timeout = timeout;
        this.message = message;
    }


    @Override
    public String getAction() {
        return action;
    }

    @Override
    public int getLimit() {
        return limit;
    }

    @Override
    public Duration getTimeout() {
        return timeout;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
