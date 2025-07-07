package com.jobflow.notification_service.notification;

import com.jobflow.notification_service.TestUtil;
import com.jobflow.notification_service.telegram.TelegramService;
import com.jobflow.notification_service.user.UserClient;
import com.jobflow.notification_service.user.UserInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TelegramNotificationServiceTest {

    @Mock
    private UserClient userClient;

    @Mock
    private TelegramService telegramService;

    @InjectMocks
    private TelegramNotificationService telegramNotificationService;

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

        telegramNotificationService.send(notificationEvent);

        verify(telegramService, times(1)).send(
                userInfo.getTelegramChatId(), notificationEvent.getMessage()
        );
    }

    @Test
    public void send_withoutTelegramChatId_doesNotSendNotificationEvent() {
        userInfo.setTelegramChatId(null);
        when(userClient.getUserInfo(notificationEvent.getUserId())).thenReturn(userInfo);

        telegramNotificationService.send(notificationEvent);

        verify(telegramService, never()).send(anyLong(), anyString());
    }
}