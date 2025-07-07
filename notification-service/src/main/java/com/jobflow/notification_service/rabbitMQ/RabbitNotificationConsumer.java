package com.jobflow.notification_service.rabbitMQ;

import com.jobflow.notification_service.notification.NotificationEvent;
import com.jobflow.notification_service.notification.NotificationService;

public abstract class RabbitNotificationConsumer extends RabbitAbstractConsumer<NotificationEvent> {

    protected final NotificationService notificationService;

    public RabbitNotificationConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
}
