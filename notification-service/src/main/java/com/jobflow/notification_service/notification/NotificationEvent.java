package com.jobflow.notification_service.notification;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class NotificationEvent {

    private Long userId;

    private NotificationType notificationType;

    private String subject; //subject for email

    private String message;
}
