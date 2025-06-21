package com.jobflow.notification_service;

import com.jobflow.notification_service.notification.NotificationEvent;
import com.jobflow.notification_service.notification.NotificationType;
import com.jobflow.notification_service.user.UserInfo;

/**
 * A utility class for use in unit/integration tests
 * and avoid repeatable code.
 */
public final class TestUtil {

    public static final Long USER_ID = 1L;

    private TestUtil() {

    }

    public static NotificationEvent createNotificationEvent() {
        return NotificationEvent.builder()
                .userId(USER_ID)
                .notificationType(NotificationType.EMAIL)
                .subject("subject")
                .message("message")
                .build();
    }

    public static UserInfo createUserInfo() {
        return UserInfo.builder()
                .email("IvanIvanov@gmail.com")
                .telegramChatId("123")
                .build();
    }
}
