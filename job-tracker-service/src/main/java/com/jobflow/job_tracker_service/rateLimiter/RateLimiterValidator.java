package com.jobflow.job_tracker_service.rateLimiter;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RateLimiterValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimiterValidator.class);

    private final RateLimiterService rateLimiterService;

    public void validate(RateLimiterAction action, String identifier) {
        LOGGER.debug("Starting rate limiter validation for action: {}", action.getAction());
        String key = RateLimiterKeyUtil.generateKey(action.getAction(), identifier);

        rateLimiterService.validateOrThrow(
                key,
                action.getLimit(),
                action.getTimeout(),
                action.getMessage()
        );
    }
}
