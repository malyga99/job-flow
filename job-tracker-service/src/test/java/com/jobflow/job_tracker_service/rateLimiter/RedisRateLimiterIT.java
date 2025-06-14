package com.jobflow.job_tracker_service.rateLimiter;

import com.jobflow.job_tracker_service.BaseIT;
import com.jobflow.job_tracker_service.exception.TooManyRequestsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

public class RedisRateLimiterIT extends BaseIT {

    @Autowired
    private RedisRateLimiterService rateLimiterService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @BeforeEach
    public void setup() {
        redisTemplate.delete("test-key");
    }

    @Test
    public void validateOrThrow_ifLimitNotExceeded_doesNotThrowExc() {
        assertDoesNotThrow(() -> rateLimiterService.validateOrThrow("test-key", 5, Duration.ofMinutes(1), "test-message"));

        String key = redisTemplate.opsForValue().get("test-key");
        assertNotNull(key);
        assertEquals("1", key);
    }

    @Test
    public void validateOrThrow_ifLimitExceeded_throwExc() {
        int limit = 5;
        for (int i = 0; i < limit; i++) {
            assertDoesNotThrow(() -> rateLimiterService.validateOrThrow("test-key", limit, Duration.ofMinutes(1), "test-message"));
        }

        var tooManyRequestsException = assertThrows(TooManyRequestsException.class, () -> rateLimiterService.validateOrThrow("test-key", limit, Duration.ofMinutes(1), "test-message"));
        assertEquals("test-message", tooManyRequestsException.getMessage());

        String key = redisTemplate.opsForValue().get("test-key");
        assertNotNull(key);
        assertEquals("6", key);
    }

    @Test
    public void validateOrThrow_whenWindowResets_restartLimit() {
        int limit = 3;
        Duration timeout = Duration.ofSeconds(3);

        for (int i = 0; i < limit; i++) {
            assertDoesNotThrow(() -> rateLimiterService.validateOrThrow("test-key", limit, timeout, "test-message"));
        }

        assertThrows(TooManyRequestsException.class, () -> rateLimiterService.validateOrThrow("test-key", limit, timeout, "test-message"));

        await().atMost(timeout.plusSeconds(1))
                .until(() -> redisTemplate.getExpire("test-key") == -2); //-2 means that key does not exist
        assertDoesNotThrow(() -> rateLimiterService.validateOrThrow("test-key", limit, timeout, "test-message"));

        String key = redisTemplate.opsForValue().get("test-key");
        assertNotNull(key);
        assertEquals("1", key);
    }
}
