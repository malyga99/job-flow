package com.jobflow.notification_service.rabbitMQ;

import com.jobflow.notification_service.TestUtil;
import com.jobflow.notification_service.notification.AbstractNotificationService;
import com.jobflow.notification_service.notification.NotificationEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RabbitTelegramConsumerTest {

    @Mock
    private AbstractNotificationService<?> notificationService;

    @InjectMocks
    private RabbitTelegramConsumer telegramConsumer;

    private NotificationEvent notificationEvent;

    @BeforeEach
    public void setup() {
        notificationEvent = TestUtil.createNotificationEvent();
    }

    @Test
    public void consume_consumeNotificationEvent() {
        telegramConsumer.consume(notificationEvent);

        verify(notificationService, times(1)).send(notificationEvent);
    }

}