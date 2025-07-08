package com.jobflow.notification_service.notification;

import com.jobflow.notification_service.TestUtil;
import com.jobflow.notification_service.email.EmailService;
import com.jobflow.notification_service.notification.history.NotificationHistoryRepository;
import com.jobflow.notification_service.notification.history.NotificationHistoryService;
import com.jobflow.notification_service.user.UserClient;
import com.jobflow.notification_service.user.UserInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailNotificationServiceTest {

    @Mock
    private UserClient userClient;

    @Mock
    private NotificationHistoryService notificationHistoryService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmailNotificationService emailNotificationService;

    private NotificationEvent notificationEvent;

    private UserInfo userInfo;

    @BeforeEach
    public void setup() {
        notificationEvent = TestUtil.createNotificationEvent();

        userInfo = TestUtil.createUserInfo();
    }

    @Test
    public void extractContact_extractEmail() {
        String email = emailNotificationService.extractContact(userInfo);

        assertEquals(userInfo.getEmail(), email);
    }

    @Test
    public void sendNotification_delegateToEmailService() {
        emailNotificationService.sendNotification(userInfo.getEmail(), notificationEvent);

        verify(emailService, times(1)).send(
                userInfo.getEmail(),
                notificationEvent.getSubject(),
                notificationEvent.getMessage()
        );
    }

    @Test
    public void getNotificationType_returnEmail() {
        NotificationType notificationType = emailNotificationService.getNotificationType();

        assertEquals(NotificationType.EMAIL, notificationType);
    }


}