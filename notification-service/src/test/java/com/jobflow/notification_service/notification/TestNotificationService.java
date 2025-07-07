package com.jobflow.notification_service.notification;

import com.jobflow.notification_service.notification.history.NotificationHistoryRepository;
import com.jobflow.notification_service.user.UserClient;
import com.jobflow.notification_service.user.UserInfo;

public class TestNotificationService extends AbstractNotificationService<String> {

    public TestNotificationService(UserClient userClient, NotificationHistoryRepository notificationHistoryRepository) {
        super(userClient, notificationHistoryRepository);
    }

    @Override
    protected String extractContact(UserInfo userInfo) {
        return userInfo.getEmail();
    }

    @Override
    protected void sendNotification(String contact, NotificationEvent notificationEvent) {

    }

    @Override
    protected NotificationType getNotificationType() {
        return NotificationType.EMAIL;
    }
}
