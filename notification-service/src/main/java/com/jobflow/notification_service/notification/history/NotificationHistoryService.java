package com.jobflow.notification_service.notification.history;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationHistoryService {

    Page<NotificationHistoryDto> findMy(Pageable pageable);
}
