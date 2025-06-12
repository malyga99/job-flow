package com.jobflow.job_tracker_service.rabbitMQ;

import com.jobflow.job_tracker_service.TestUtil;
import com.jobflow.job_tracker_service.notification.NotificationEvent;
import com.jobflow.job_tracker_service.notification.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpTemplate;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RabbitEmailServiceTest {

    @Mock
    private AmqpTemplate amqpTemplate;

    @Mock
    private RabbitProperties rabbitProperties;

    private RabbitEmailService rabbitEmailService;

    private NotificationEvent notificationEvent;

    @BeforeEach
    public void setup() {
        when(rabbitProperties.getExchangeName()).thenReturn("test-exchange");
        when(rabbitProperties.getEmailQueueRoutingKey()).thenReturn("test-key");

        rabbitEmailService = new RabbitEmailService(amqpTemplate, rabbitProperties);

        notificationEvent = TestUtil.createNotificationEvent();
    }

    @Test
    public void send_sendEventSuccessfully() {
        rabbitEmailService.send(notificationEvent);

        verify(amqpTemplate, times(1)).convertAndSend("test-exchange",
                "test-key", notificationEvent);
    }

    @Test
    public void getType_returnEmailType() {
        NotificationType result = rabbitEmailService.getType();

        assertEquals(NotificationType.EMAIL, result);
    }

}