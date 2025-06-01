package com.jobflow.job_tracker_service.jobApplication.stats;

public final class StatsCacheKeyUtils {

    private static final String STATS_KEY = "jobAppStats:user:%s";

    private StatsCacheKeyUtils() {

    }

    public static String keyForUser(Long userId) {
        return String.format(STATS_KEY, userId);
    }
}
