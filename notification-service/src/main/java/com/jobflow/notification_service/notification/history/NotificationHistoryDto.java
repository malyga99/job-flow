package com.jobflow.notification_service.notification.history;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jobflow.notification_service.notification.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO representing a notification history details")
public class NotificationHistoryDto {

    @Schema(description = "Notification type. Where notifications are sent to", example = "EMAIL")
    private NotificationType notificationType;

    @Schema(description = "Subject of the notification. Used for sending by email", example = "subject")
    private String subject;

    @Schema(description = "A message describing the notification", example = "message")
    private String message;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @Schema(description = "Date the notification was sent", example = "2023-01-01 12:00")
    private LocalDateTime createdAt;
}
