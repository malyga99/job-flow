package com.jobflow.job_tracker_service.rateLimiter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RateLimiterValidatorTest {

    @Mock
    private RateLimiterService rateLimiterService;

    @Mock
    private RateLimiterAction rateLimiterAction;

    @InjectMocks
    private RateLimiterValidator rateLimiterValidator;

    @Test
    public void validate_validateRateLimiterCorrectly() {
        when(rateLimiterAction.getAction()).thenReturn("test-action");
        when(rateLimiterAction.getLimit()).thenReturn(5);
        when(rateLimiterAction.getTimeout()).thenReturn(Duration.ofMinutes(1L));
        when(rateLimiterAction.getMessage()).thenReturn("test-message");

        rateLimiterValidator.validate(rateLimiterAction, "test-identifier");

        verify(rateLimiterService, times(1)).validateOrThrow(
                RateLimiterKeyUtil.generateKey("test-action", "test-identifier"),
                5,
                Duration.ofMinutes(1),
                "test-message"
        );
    }
}