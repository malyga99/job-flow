package com.jobflow.notification_service.notification;

import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService implements NotificationService {

    @Override
    public void send(NotificationEvent notificationEvent) {
        System.out.println(notificationEvent);
    }
}
