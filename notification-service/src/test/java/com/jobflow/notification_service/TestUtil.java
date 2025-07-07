package com.jobflow.notification_service;

import com.jobflow.notification_service.notification.NotificationEvent;
import com.jobflow.notification_service.notification.NotificationType;
import com.jobflow.notification_service.notification.history.NotificationHistory;
import com.jobflow.notification_service.notification.history.NotificationHistoryDto;
import com.jobflow.notification_service.telegram.TelegramChat;
import com.jobflow.notification_service.telegram.TelegramMessage;
import com.jobflow.notification_service.telegram.TelegramUpdate;
import com.jobflow.notification_service.telegram.TelegramUser;
import com.jobflow.notification_service.user.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import java.time.LocalDateTime;

/**
 * A utility class for use in unit/integration tests
 * and avoid repeatable code.
 */
public final class TestUtil {

    public static final Long USER_ID = 1L;

    private TestUtil() {

    }

    public static <T> HttpEntity<T> createRequest(T request, HttpHeaders headers) {
        return new HttpEntity<>(request, headers);
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
                .telegramChatId(1L)
                .build();
    }

    public static TelegramUpdate createTelegramUpdate() {
        TelegramChat chat = TelegramChat.builder()
                .id(1L)
                .type("private")
                .build();
        TelegramUser user = TelegramUser.builder()
                .id(1L)
                .is_bot(Boolean.FALSE)
                .first_name("Ivan")
                .build();
        TelegramMessage message = TelegramMessage.builder()
                .text("some-text")
                .chat(chat)
                .from(user)
                .build();

        return TelegramUpdate.builder()
                .message(message)
                .build();
    }

    public static NotificationHistoryDto createNotificationHistoryDto() {
        return NotificationHistoryDto.builder()
                .notificationType(NotificationType.EMAIL)
                .subject("test-subject")
                .message("test-message")
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static NotificationHistory createNotificationHistory() {
        return NotificationHistory.builder()
                .id(1L)
                .userId(1L)
                .notificationType(NotificationType.EMAIL)
                .subject("test-subject")
                .message("test-message")
                .success(true)
                .failureReason(null)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static void clearDb(JpaRepository<?, ?> repository) {
        repository.deleteAll();
    }
}
