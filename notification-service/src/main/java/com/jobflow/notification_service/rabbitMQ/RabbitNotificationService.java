package com.jobflow.notification_service.rabbitMQ;

import com.jobflow.notification_service.notification.NotificationEvent;
import com.jobflow.notification_service.notification.NotificationService;

public abstract class RabbitNotificationService extends RabbitAbstractService<NotificationEvent> {

    protected final NotificationService notificationService;

    public RabbitNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
}
