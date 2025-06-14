package com.jobflow.job_tracker_service.rateLimiter;

import com.jobflow.job_tracker_service.exception.TooManyRequestsException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisRateLimiterService implements RateLimiterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisRateLimiterService.class);

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public boolean isLimitExceeded(String key, int limit, Duration timeout) {
        Long count = redisTemplate.opsForValue().increment(key);
        LOGGER.debug("Rate limiter checking for key [{}]: count = {}, limit = {}, timeout = {}s",
                key, count, limit, timeout.getSeconds());

        if (count == 1) {
            redisTemplate.expire(key, timeout);
        }

        return count > limit;
    }

    @Override
    public void validateOrThrow(String key, int limit, Duration timeout, String message) {
        if (isLimitExceeded(key, limit, timeout)) {
            throw new TooManyRequestsException(message);
        }
    }
}
