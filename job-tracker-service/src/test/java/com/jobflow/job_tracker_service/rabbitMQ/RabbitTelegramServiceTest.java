package com.jobflow.job_tracker_service.rabbitMQ;

import com.jobflow.job_tracker_service.TestUtil;
import com.jobflow.job_tracker_service.notification.NotificationEvent;
import com.jobflow.job_tracker_service.notification.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RabbitTelegramServiceTest {

    @Mock
    private AmqpTemplate amqpTemplate;

    @Mock
    private RabbitProperties rabbitProperties;

    private RabbitTelegramService rabbitTelegramService;

    private NotificationEvent notificationEvent;

    @BeforeEach
    public void setup() {
        when(rabbitProperties.getExchangeName()).thenReturn("test-exchange");
        when(rabbitProperties.getTelegramQueueRoutingKey()).thenReturn("test-key");

        rabbitTelegramService = new RabbitTelegramService(amqpTemplate, rabbitProperties);

        notificationEvent = TestUtil.createNotificationEvent();
    }

    @Test
    public void send_sendEventSuccessfully() {
        rabbitTelegramService.send(notificationEvent);

        verify(amqpTemplate, times(1)).convertAndSend("test-exchange",
                "test-key", notificationEvent);
    }

    @Test
    public void getType_returnTelegramType() {
        NotificationType result = rabbitTelegramService.getType();

        assertEquals(NotificationType.TELEGRAM, result);
    }

}