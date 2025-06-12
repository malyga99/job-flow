package com.jobflow.job_tracker_service.rabbitMQ;

import com.jobflow.job_tracker_service.notification.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RabbitNotificationServiceTest {

    private RabbitNotificationService rabbitNotificationService;

    @BeforeEach
    public void setup() {
        rabbitNotificationService = mock(RabbitNotificationService.class, Answers.CALLS_REAL_METHODS);
    }

    @Test
    public void supports_ifTypeEquals_returnTrue() {
        when(rabbitNotificationService.getType()).thenReturn(NotificationType.TELEGRAM);

        boolean result = rabbitNotificationService.supports(NotificationType.TELEGRAM);

        assertTrue(result);
    }

    @Test
    public void supports_ifTypeDoesNotEquals_returnFalse() {
        when(rabbitNotificationService.getType()).thenReturn(NotificationType.EMAIL);

        boolean result = rabbitNotificationService.supports(NotificationType.TELEGRAM);

        assertFalse(result);
    }

    @Test
    public void supports_ifTypeIsBoth_returnTrue() {
        when(rabbitNotificationService.getType()).thenReturn(NotificationType.EMAIL);

        boolean result = rabbitNotificationService.supports(NotificationType.BOTH);

        assertTrue(result);
    }

}