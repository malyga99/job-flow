package com.jobflow.notification_service.notification;

import com.jobflow.notification_service.TestUtil;
import com.jobflow.notification_service.email.EmailService;
import com.jobflow.notification_service.user.UserClient;
import com.jobflow.notification_service.user.UserInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailNotificationServiceTest {

    @Mock
    private UserClient userClient;

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
    public void send_sendNotificationEvent() {
        when(userClient.getUserInfo(notificationEvent.getUserId())).thenReturn(userInfo);

        emailNotificationService.send(notificationEvent);

        verify(emailService, times(1)).send(
                userInfo.getEmail(),
                notificationEvent.getSubject(),
                notificationEvent.getMessage()
        );
    }

    @Test
    public void send_withoutEmail_doesNotSendNotificationEvent() {
        userInfo.setEmail(null);
        when(userClient.getUserInfo(notificationEvent.getUserId())).thenReturn(userInfo);

        emailNotificationService.send(notificationEvent);

        verify(emailService, never()).send(anyString(), anyString(), anyString());
    }

}