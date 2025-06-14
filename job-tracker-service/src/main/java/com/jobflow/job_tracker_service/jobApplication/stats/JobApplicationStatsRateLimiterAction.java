package com.jobflow.job_tracker_service.jobApplication.stats;

import com.jobflow.job_tracker_service.rateLimiter.RateLimiterAction;

import java.time.Duration;

public enum JobApplicationStatsRateLimiterAction implements RateLimiterAction {

    GET_STATS("stats", 5, Duration.ofMinutes(1), "Too many stats requests. Try again in a minute");

    private final String action;
    private final int limit;
    private final Duration timeout;
    private final String message;

    JobApplicationStatsRateLimiterAction(String action, int limit, Duration timeout, String message) {
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
