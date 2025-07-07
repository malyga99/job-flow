package com.jobflow.notification_service.notification;

import com.jobflow.notification_service.notification.history.NotificationHistory;
import com.jobflow.notification_service.notification.history.NotificationHistoryRepository;
import com.jobflow.notification_service.user.UserClient;
import com.jobflow.notification_service.user.UserInfo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
public abstract class AbstractNotificationService<C> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractNotificationService.class);

    private final UserClient userClient;
    private final NotificationHistoryRepository notificationHistoryRepository;

    public void send(NotificationEvent notificationEvent) {
        Long userId = notificationEvent.getUserId();
        LOGGER.debug("Sending notification event to user with id: {}", userId);

        try {
            UserInfo userInfo = userClient.getUserInfo(userId);
            C contact = extractContact(userInfo);

            if (contact != null) {
                sendNotification(contact, notificationEvent);
                saveNotificationHistory(notificationEvent, true, null);

                LOGGER.debug("Successfully send notification event to user with id: {}, destination: {}",
                        userId, contact);
            } else {
                saveNotificationHistory(notificationEvent, false, "Contact data not found");

                LOGGER.debug("Contact data was not found. Notification event not sent to user with id: {}", userId);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to sending notification event to user with id: {}, message: {}",
                    userId, e.getMessage());

            saveNotificationHistory(notificationEvent, false, e.getMessage());
        }
    }

    protected abstract C extractContact(UserInfo userInfo);

    protected abstract void sendNotification(C contact, NotificationEvent notificationEvent);

    protected abstract NotificationType getNotificationType();

    private void saveNotificationHistory(NotificationEvent notificationEvent, boolean success, String failureReason) {
        notificationHistoryRepository.save(NotificationHistory.builder()
                .userId(notificationEvent.getUserId())
                .notificationType(getNotificationType())
                .subject(notificationEvent.getSubject())
                .message(notificationEvent.getMessage())
                .success(success)
                .failureReason(failureReason)
                .build());
    }
}
