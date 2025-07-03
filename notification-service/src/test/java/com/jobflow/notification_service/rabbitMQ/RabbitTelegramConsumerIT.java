package com.jobflow.notification_service.rabbitMQ;

import com.jobflow.notification_service.BaseIT;
import com.jobflow.notification_service.TestUtil;
import com.jobflow.notification_service.notification.NotificationEvent;
import com.jobflow.notification_service.telegram.TelegramProperties;
import com.jobflow.notification_service.user.UserInfo;
import com.jobflow.notification_service.user.UserServiceProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

public class RabbitTelegramConsumerIT extends BaseIT {

    @Autowired
    private UserServiceProperties userServiceProperties;

    @Autowired
    private TelegramProperties telegramProperties;

    @Autowired
    private RabbitProperties rabbitProperties;

    @Autowired
    private AmqpTemplate amqpTemplate;

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
    public void consume_consumeNotificationEventCorrectlyAndSendToTelegramSuccessfully() {
        ArgumentCaptor<HttpEntity<Map<String, String>>> captor = ArgumentCaptor.forClass(HttpEntity.class);
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
                rabbitProperties.getTelegramQueueRoutingKey(),
                notificationEvent
        );

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
                verify(restTemplate, times(1)).exchange(
                        eq(String.format("https://api.telegram.org/bot%s/sendMessage", telegramProperties.getBotToken())),
                        eq(HttpMethod.POST),
                        captor.capture(),
                        eq(Void.class)
                ));

        HttpEntity<Map<String, String>> request = captor.getValue();
        Map<String, String> body = request.getBody();
        assertNotNull(body);
        assertEquals(String.valueOf(userInfo.getTelegramChatId()), body.get("chat_id"));
        assertEquals(notificationEvent.getMessage(), body.get("text"));
    }
}
