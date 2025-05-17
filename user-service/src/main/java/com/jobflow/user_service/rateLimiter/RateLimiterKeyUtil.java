package com.jobflow.user_service.rateLimiter;

public final class RateLimiterKeyUtil {

    private RateLimiterKeyUtil() {

    }

    public static String generateKey(String endpoint, String identifier) {
        return String.format("rate_limiter:%s:%s", endpoint, identifier);
    }

    public static String generateIpKey(String endpoint, String identifier, String ip) {
        return generateKey(endpoint, identifier) + ":" + ip;
    }

    public static String generateIpKey(String endpoint, String ip) {
        return generateKey(endpoint, ip);
    }

}
