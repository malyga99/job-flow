package com.jobflow.notification_service.rabbitMQ;

import com.jobflow.notification_service.BaseIT;
import com.jobflow.notification_service.TestUtil;
import com.jobflow.notification_service.email.EmailService;
import com.jobflow.notification_service.exception.UserClientException;
import com.jobflow.notification_service.notification.NotificationEvent;
import com.jobflow.notification_service.notification.NotificationType;
import com.jobflow.notification_service.notification.history.NotificationHistory;
import com.jobflow.notification_service.notification.history.NotificationHistoryRepository;
import com.jobflow.notification_service.user.UserInfo;
import com.jobflow.notification_service.user.UserServiceProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.testcontainers.shaded.org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RabbitEmailConsumerIT extends BaseIT {

    @Autowired
    private UserServiceProperties userServiceProperties;

    @Autowired
    private RabbitProperties rabbitProperties;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private NotificationHistoryRepository historyRepository;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private RestTemplate restTemplate;

    private NotificationEvent notificationEvent;

    private UserInfo userInfo;

    @BeforeEach
    public void setup() {
        TestUtil.clearDb(historyRepository);
        TestUtil.clearRabbit(amqpAdmin, rabbitProperties.getEmailDlqName());

        notificationEvent = TestUtil.createNotificationEvent();

        userInfo = TestUtil.createUserInfo();
    }

    @Test
    public void consume_consumeNotificationEventAndSendEmailSuccessfully() {
        mockFetchingUserInfo();
        sendMessageInQueue();

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
                verify(emailService, times(1)).send(
                        userInfo.getEmail(),
                        notificationEvent.getSubject(),
                        notificationEvent.getMessage()
                ));
    }

    @Test
    public void consume_ifSuccess_saveInDbHistory() {
        consume_consumeNotificationEventAndSendEmailSuccessfully();

        List<NotificationHistory> notifications = historyRepository.findAll();
        assertEquals(1, notifications.size());

        NotificationHistory savedNotification = notifications.get(0);
        assertNotNull(savedNotification);
        assertNotNull(savedNotification.getId());
        assertEquals(notificationEvent.getUserId(), savedNotification.getUserId());
        assertEquals(NotificationType.EMAIL, savedNotification.getNotificationType());
        assertEquals(notificationEvent.getSubject(), savedNotification.getSubject());
        assertEquals(notificationEvent.getMessage(), savedNotification.getMessage());
        assertTrue(savedNotification.getSuccess());
        assertNull(savedNotification.getFailureReason());
        assertNotNull(savedNotification.getCreatedAt());
    }

    @Test
    public void consume_ifContactDataNotFound_saveInDbHistory() {
        userInfo.setEmail(null);

        mockFetchingUserInfo();
        sendMessageInQueue();

        await().atMost(5, TimeUnit.SECONDS).until(() ->
                historyRepository.findAll().size() == 1);

        NotificationHistory savedNotification = historyRepository.findAll().get(0);
        assertNotNull(savedNotification);
        assertNotNull(savedNotification.getId());
        assertEquals(notificationEvent.getUserId(), savedNotification.getUserId());
        assertEquals(NotificationType.EMAIL, savedNotification.getNotificationType());
        assertEquals(notificationEvent.getSubject(), savedNotification.getSubject());
        assertEquals(notificationEvent.getMessage(), savedNotification.getMessage());
        assertFalse(savedNotification.getSuccess());
        assertEquals("Contact data not found", savedNotification.getFailureReason());
        assertNotNull(savedNotification.getCreatedAt());
    }

    @Test
    public void consume_ifSomeExc_saveInDbHistoryAfterRetries() {
        var userClientException = new UserClientException("User client exception");

        when(restTemplate.exchange(
                eq(String.format("http://%s:%s/api/v1/users/info?userId=%s",
                        userServiceProperties.getHost(), userServiceProperties.getPort(), notificationEvent.getUserId())),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(UserInfo.class)
        )).thenThrow(userClientException);
        sendMessageInQueue();


        int maxAttempts = Integer.parseInt(rabbitProperties.getRetryableMaxAttempts());
        int waitTimeMillis = Integer.parseInt(rabbitProperties.getRetryableDelay()) * maxAttempts;
        await().atMost(waitTimeMillis + 3000, TimeUnit.MILLISECONDS).until(() ->
                historyRepository.findAll().size() == maxAttempts);

        List<NotificationHistory> notifications = historyRepository.findAll();

        notifications.forEach(savedNotification -> {
            assertNotNull(savedNotification);
            assertNotNull(savedNotification.getId());
            assertEquals(notificationEvent.getUserId(), savedNotification.getUserId());
            assertEquals(NotificationType.EMAIL, savedNotification.getNotificationType());
            assertEquals(notificationEvent.getSubject(), savedNotification.getSubject());
            assertEquals(notificationEvent.getMessage(), savedNotification.getMessage());
            assertFalse(savedNotification.getSuccess());
            assertEquals(userClientException.getMessage(), savedNotification.getFailureReason());
            assertNotNull(savedNotification.getCreatedAt());
        });
    }

    @Test
    public void consume_ifSomeExc_sendingEventInDlqCorrectly() {
        var userClientException = new UserClientException("User client exception");

        when(restTemplate.exchange(
                eq(String.format("http://%s:%s/api/v1/users/info?userId=%s",
                        userServiceProperties.getHost(), userServiceProperties.getPort(), notificationEvent.getUserId())),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(UserInfo.class)
        )).thenThrow(userClientException);
        sendMessageInQueue();

        int maxAttempts = Integer.parseInt(rabbitProperties.getRetryableMaxAttempts());
        int waitTimeMillis = Integer.parseInt(rabbitProperties.getRetryableDelay()) * maxAttempts;

        NotificationEvent eventFromDlq = amqpTemplate.receiveAndConvert(
                rabbitProperties.getEmailDlqName(), waitTimeMillis, new ParameterizedTypeReference<NotificationEvent>() {
                });

        assertNotNull(eventFromDlq);
        assertEquals(eventFromDlq.getUserId(), notificationEvent.getUserId());
        assertEquals(eventFromDlq.getNotificationType(), notificationEvent.getNotificationType());
        assertEquals(eventFromDlq.getSubject(), notificationEvent.getSubject());
        assertEquals(eventFromDlq.getMessage(), notificationEvent.getMessage());
    }

    private void mockFetchingUserInfo() {
        ResponseEntity<UserInfo> responseUserService = new ResponseEntity<>(userInfo, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(String.format("http://%s:%s/api/v1/users/info?userId=%s",
                        userServiceProperties.getHost(), userServiceProperties.getPort(), notificationEvent.getUserId())),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(UserInfo.class)
        )).thenReturn(responseUserService);
    }

    private void sendMessageInQueue() {
        amqpTemplate.convertAndSend(
                rabbitProperties.getExchangeName(),
                rabbitProperties.getEmailQueueRoutingKey(),
                notificationEvent
        );
    }

}
