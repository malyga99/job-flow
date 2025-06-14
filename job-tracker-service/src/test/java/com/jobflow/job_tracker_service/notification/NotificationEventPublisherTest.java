package com.jobflow.job_tracker_service.notification;

import com.jobflow.job_tracker_service.TestUtil;
import com.jobflow.job_tracker_service.rabbitMQ.RabbitNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationEventPublisherTest {

    @Mock
    private RabbitNotificationService emailNotificationService;

    @Mock
    private RabbitNotificationService telegramNotificationService;

    private NotificationEventPublisher eventPublisher;

    private NotificationEvent notificationEvent;

    @BeforeEach
    public void setup() {
        when(emailNotificationService.getType()).thenReturn(NotificationType.EMAIL);
        when(telegramNotificationService.getType()).thenReturn(NotificationType.TELEGRAM);
        eventPublisher = new NotificationEventPublisher(List.of(emailNotificationService, telegramNotificationService));

        notificationEvent = TestUtil.createNotificationEvent();
    }

    @Test
    public void publish_publishEventSuccessfully() {
        when(emailNotificationService.supports(notificationEvent.getNotificationType())).thenReturn(true);
        when(telegramNotificationService.supports(notificationEvent.getNotificationType())).thenReturn(true);

        eventPublisher.publish(notificationEvent);

        verify(emailNotificationService, times(1)).send(notificationEvent);
        verify(telegramNotificationService, times(1)).send(notificationEvent);
    }

    @Test
    public void publish_whenAnyServiceDoesNotSupported_doesNotPublishEvent() {
        when(emailNotificationService.supports(notificationEvent.getNotificationType())).thenReturn(true);
        when(telegramNotificationService.supports(notificationEvent.getNotificationType())).thenReturn(false);

        eventPublisher.publish(notificationEvent);

        verify(emailNotificationService, times(1)).send(notificationEvent);
        verify(telegramNotificationService, never()).send(notificationEvent);
    }

}