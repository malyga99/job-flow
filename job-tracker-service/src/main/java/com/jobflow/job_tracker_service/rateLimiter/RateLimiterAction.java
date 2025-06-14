package com.jobflow.job_tracker_service.rateLimiter;

import java.time.Duration;

public interface RateLimiterAction {

    String getAction();

    int getLimit();

    Duration getTimeout();

    String getMessage();
}
