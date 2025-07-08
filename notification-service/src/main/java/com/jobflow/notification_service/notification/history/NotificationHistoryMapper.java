package com.jobflow.notification_service.notification.history;

import org.springframework.stereotype.Component;

@Component
public class NotificationHistoryMapper {

    public NotificationHistoryDto toDto(NotificationHistory notificationHistory) {
        return NotificationHistoryDto.builder()
                .notificationType(notificationHistory.getNotificationType())
                .subject(notificationHistory.getSubject())
                .message(notificationHistory.getMessage())
                .createdAt(notificationHistory.getCreatedAt())
                .build();
    }
}
