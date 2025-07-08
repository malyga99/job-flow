package com.jobflow.notification_service.notification.history;

import com.jobflow.notification_service.notification.NotificationEvent;
import com.jobflow.notification_service.notification.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationHistoryService {

    Page<NotificationHistoryDto> findMy(Pageable pageable);

    void save(NotificationEvent notificationEvent, NotificationType notificationType, boolean success, String failureReason);
}
