package com.jobflow.notification_service.notification.history;

import com.jobflow.notification_service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationHistoryServiceImpl implements NotificationHistoryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationHistoryServiceImpl.class);

    private final UserService userService;
    private final NotificationHistoryRepository notificationHistoryRepository;
    private final NotificationHistoryMapper notificationHistoryMapper;

    @Override
    public Page<NotificationHistoryDto> findMy(Pageable pageable) {
        Long currentUserId = userService.getCurrentUserId();
        LOGGER.debug("Fetching notifications of the current user with id: {}", currentUserId);

        Page<NotificationHistory> notifications =
                notificationHistoryRepository.findByUserIdAndSuccess(currentUserId, true, pageable);

        LOGGER.debug("Fetched: {} notifications of the current user with id: {}",
                notifications.getContent().size(), currentUserId);
        return notifications.map(notificationHistoryMapper::toDto);
    }
}
