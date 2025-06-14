package com.jobflow.job_tracker_service.rateLimiter;

public final class RateLimiterKeyUtil {

    private RateLimiterKeyUtil() {

    }

    public static String generateKey(String endpoint, String identifier) {
        return String.format("rate_limiter:%s:%s", endpoint, identifier);
    }
}

