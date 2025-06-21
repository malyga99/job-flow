package com.jobflow.notification_service.rabbitMQ;

import com.jobflow.notification_service.BaseIT;
import com.jobflow.notification_service.TestUtil;
import com.jobflow.notification_service.email.EmailService;
import com.jobflow.notification_service.notification.NotificationEvent;
import com.jobflow.notification_service.user.UserClient;
import com.jobflow.notification_service.user.UserInfo;
import com.jobflow.notification_service.user.UserServiceProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

public class RabbitEmailConsumerIT extends BaseIT {

    @Autowired
    private UserServiceProperties userServiceProperties;

    @Autowired
    private RabbitProperties rabbitProperties;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private RestTemplate restTemplate;

    private NotificationEvent notificationEvent;

    private UserInfo userInfo;

    @BeforeEach
    public void setup() {
        notificationEvent = TestUtil.createNotificationEvent();
        userInfo = TestUtil.createUserInfo();
    }

    @Test
    public void consume_consumeNotificationEventAndSendEmailSuccessfully() {
        ResponseEntity<UserInfo> responseUserService = new ResponseEntity<>(userInfo, HttpStatus.OK);
        when(restTemplate.exchange(
                eq(String.format("http://%s:%s/api/v1/users/info?userId=%s",
                        userServiceProperties.getHost(), userServiceProperties.getPort(), notificationEvent.getUserId())),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(UserInfo.class)
        )).thenReturn(responseUserService);

        amqpTemplate.convertAndSend(
                rabbitProperties.getExchangeName(),
                rabbitProperties.getEmailQueueRoutingKey(),
                notificationEvent
        );

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
                verify(emailService, times(1)).send(
                        userInfo.getEmail(),
                        notificationEvent.getSubject(),
                        notificationEvent.getMessage()
                ));
    }

}
