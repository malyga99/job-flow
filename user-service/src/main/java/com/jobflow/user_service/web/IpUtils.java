package com.jobflow.user_service.web;

import jakarta.servlet.http.HttpServletRequest;

public final class IpUtils {

    private IpUtils() {

    }

    public static String extractClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");

        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0];
        }

        return request.getRemoteAddr();
    }
}
