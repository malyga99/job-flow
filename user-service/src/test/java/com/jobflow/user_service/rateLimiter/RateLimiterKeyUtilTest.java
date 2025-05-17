package com.jobflow.user_service.rateLimiter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RateLimiterKeyUtilTest {

    @Test
    public void generateKey_returnCorrectlyKey() {
        String result = RateLimiterKeyUtil.generateKey("auth", "IvanIvanov@gmail.com");

        assertNotNull(result);
        assertEquals("rate_limiter:auth:IvanIvanov@gmail.com", result);
    }

    @Test
    public void generateIpKey_returnCorrectlyKey() {
        String result = RateLimiterKeyUtil.generateIpKey("auth", "IvanIvanov@gmail.com", "127.0.0.1");

        assertNotNull(result);
        assertEquals("rate_limiter:auth:IvanIvanov@gmail.com:127.0.0.1", result);
    }

    @Test
    public void generateIpKeyWithoutIdentifier_returnCorrectlyKey() {
        String result = RateLimiterKeyUtil.generateIpKey("auth", "127.0.0.1");

        assertNotNull(result);
        assertEquals("rate_limiter:auth:127.0.0.1", result);
    }

}