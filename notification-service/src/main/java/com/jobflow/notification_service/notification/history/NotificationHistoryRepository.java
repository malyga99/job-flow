package com.jobflow.notification_service.notification.history;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationHistoryRepository extends JpaRepository<NotificationHistory, Long> {

    Page<NotificationHistory> findByUserIdAndSuccess(Long userId, Boolean success, Pageable pageable);
}
