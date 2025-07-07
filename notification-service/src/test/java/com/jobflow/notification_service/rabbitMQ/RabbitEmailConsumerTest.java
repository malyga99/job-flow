package com.jobflow.notification_service.rabbitMQ;

import com.jobflow.notification_service.TestUtil;
import com.jobflow.notification_service.notification.NotificationEvent;
import com.jobflow.notification_service.notification.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RabbitEmailConsumerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private RabbitEmailConsumer emailConsumer;

    private NotificationEvent notificationEvent;

    @BeforeEach
    public void setup() {
        notificationEvent = TestUtil.createNotificationEvent();
    }

    @Test
    public void consume_consumeNotificationEvent() {
        emailConsumer.consume(notificationEvent);

        verify(notificationService, times(1)).send(notificationEvent);
    }
}