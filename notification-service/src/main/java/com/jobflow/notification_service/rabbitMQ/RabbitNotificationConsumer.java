package com.jobflow.notification_service.rabbitMQ;

import com.jobflow.notification_service.notification.AbstractNotificationService;
import com.jobflow.notification_service.notification.NotificationEvent;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class RabbitNotificationConsumer extends RabbitAbstractConsumer<NotificationEvent> {

    protected final AbstractNotificationService<?> notificationService;
}
