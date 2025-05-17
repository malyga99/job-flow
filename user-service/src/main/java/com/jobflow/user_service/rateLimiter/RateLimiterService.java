package com.jobflow.user_service.rateLimiter;

import java.time.Duration;

public interface RateLimiterService {

    boolean isLimitExceeded(String key, int limit, Duration timeout);

    void validateOrThrow(String key, int limit, Duration timeout, String message);
}
