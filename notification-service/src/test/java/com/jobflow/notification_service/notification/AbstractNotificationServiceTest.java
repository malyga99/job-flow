package com.jobflow.notification_service.notification;

import com.jobflow.notification_service.TestUtil;
import com.jobflow.notification_service.exception.UserClientException;
import com.jobflow.notification_service.notification.history.NotificationHistory;
import com.jobflow.notification_service.notification.history.NotificationHistoryRepository;
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
class AbstractNotificationServiceTest {

    @Mock
    private UserClient userClient;

    @Mock
    private NotificationHistoryRepository notificationHistoryRepository;

    @InjectMocks
    private TestNotificationService testNotificationService;

    private NotificationEvent notificationEvent;

    private UserInfo userInfo;

    @BeforeEach
    public void setup() {
        notificationEvent = TestUtil.createNotificationEvent();

        userInfo = TestUtil.createUserInfo();
    }

    @Test
    public void send_sendNotificationAndSaveInDb() {
        Long userId = notificationEvent.getUserId();
        when(userClient.getUserInfo(userId)).thenReturn(userInfo);

        testNotificationService.send(notificationEvent);

        verify(notificationHistoryRepository, times(1)).save(NotificationHistory.builder()
                        .userId(notificationEvent.getUserId())
                        .notificationType(testNotificationService.getNotificationType())
                        .subject(notificationEvent.getSubject())
                        .message(notificationEvent.getMessage())
                        .success(true)
                        .failureReason(null)
                .build());
    }

    @Test
    public void send_contactIsNull_saveInDbCorrectlyInfo() {
        userInfo.setEmail(null);
        Long userId = notificationEvent.getUserId();
        when(userClient.getUserInfo(userId)).thenReturn(userInfo);

        testNotificationService.send(notificationEvent);

        verify(notificationHistoryRepository, times(1)).save(NotificationHistory.builder()
                .userId(notificationEvent.getUserId())
                .notificationType(testNotificationService.getNotificationType())
                .subject(notificationEvent.getSubject())
                .message(notificationEvent.getMessage())
                .success(false)
                .failureReason("Contact data not found")
                .build());
    }

    @Test
    public void send_ifSomeExc_saveInDbCorrectlyInfo() {
        var userClientException = new UserClientException("User client exception");
        Long userId = notificationEvent.getUserId();
        when(userClient.getUserInfo(userId)).thenThrow(userClientException);

        testNotificationService.send(notificationEvent);

        verify(notificationHistoryRepository, times(1)).save(NotificationHistory.builder()
                .userId(notificationEvent.getUserId())
                .notificationType(testNotificationService.getNotificationType())
                .subject(notificationEvent.getSubject())
                .message(notificationEvent.getMessage())
                .success(false)
                .failureReason(userClientException.getMessage())
                .build());
    }
}