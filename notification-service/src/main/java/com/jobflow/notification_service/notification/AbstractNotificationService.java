package com.jobflow.notification_service.notification;

import com.jobflow.notification_service.exception.NotificationException;
import com.jobflow.notification_service.notification.history.NotificationHistoryService;
import com.jobflow.notification_service.user.UserClient;
import com.jobflow.notification_service.user.UserInfo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
public abstract class AbstractNotificationService<C> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractNotificationService.class);

    private final UserClient userClient;
    private final NotificationHistoryService historyService;

    public void send(NotificationEvent notificationEvent) {
        Long userId = notificationEvent.getUserId();
        NotificationType notificationType = getNotificationType();
        LOGGER.debug("Sending notification event to user with id: {}", userId);

        try {
            UserInfo userInfo = userClient.getUserInfo(userId);
            C contact = extractContact(userInfo);

            if (contact != null) {
                sendNotification(contact, notificationEvent);
                historyService.save(notificationEvent, notificationType, true, null);

                LOGGER.debug("Successfully send notification event to user with id: {}, destination: {}",
                        userId, contact);
            } else {
                historyService.save(notificationEvent, notificationType, false, "Contact data not found");

                LOGGER.debug("Contact data was not found. Notification event not sent to user with id: {}", userId);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to sending notification event to user with id: {}, message: {}",
                    userId, e.getMessage());

            historyService.save(notificationEvent, notificationType, false, e.getMessage());

            throw new NotificationException("Failed to sending notification event: " + e.getMessage(), e);
        }
    }

    protected abstract C extractContact(UserInfo userInfo);

    protected abstract void sendNotification(C contact, NotificationEvent notificationEvent);

    protected abstract NotificationType getNotificationType();
}
