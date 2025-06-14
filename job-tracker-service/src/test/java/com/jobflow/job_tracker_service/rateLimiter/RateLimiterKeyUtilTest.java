package com.jobflow.job_tracker_service.rateLimiter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RateLimiterKeyUtilTest {

    @Test
    public void generateKey_returnCorrectlyKey() {
        String result = RateLimiterKeyUtil.generateKey("create", "1");

        assertNotNull(result);
        assertEquals("rate_limiter:create:1", result);
    }
}