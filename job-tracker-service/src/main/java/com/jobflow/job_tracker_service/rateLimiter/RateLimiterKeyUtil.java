package com.jobflow.job_tracker_service.rateLimiter;

public final class RateLimiterKeyUtil {

    private RateLimiterKeyUtil() {

    }

    public static String generateKey(String action, String identifier) {
        return String.format("rate_limiter:%s:%s", action, identifier);
    }
}

