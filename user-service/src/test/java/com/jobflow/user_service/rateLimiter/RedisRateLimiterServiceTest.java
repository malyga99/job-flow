package com.jobflow.user_service.rateLimiter;

import com.jobflow.user_service.exception.TooManyRequestsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class RedisRateLimiterServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RedisRateLimiterService rateLimiterService;

    @BeforeEach
    public void setup() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    public void validateOrThrow_ifLimitNotExceeded_doesNotThrowExc() {
        when(valueOperations.increment("test-key")).thenReturn(1L);

        assertDoesNotThrow(() -> rateLimiterService.validateOrThrow("test-key", 5, Duration.ofMinutes(1), "test-message"));

        verify(redisTemplate, times(1)).expire("test-key", Duration.ofMinutes(1));
    }

    @Test
    public void validateOrThrow_ifLimitExceeded_throwExc() {
        when(valueOperations.increment("test-key")).thenReturn(6L);

        var tooManyRequestsException = assertThrows(TooManyRequestsException.class, () -> rateLimiterService.validateOrThrow("test-key", 5, Duration.ofMinutes(1), "test-message"));
        assertEquals("test-message", tooManyRequestsException.getMessage());

        verify(redisTemplate, never()).expire("test-key", Duration.ofMinutes(1));
    }
}