package com.jobflow.job_tracker_service.rabbitMQ;

import com.jobflow.job_tracker_service.notification.NotificationEvent;
import com.jobflow.job_tracker_service.notification.NotificationType;
import org.springframework.amqp.core.AmqpTemplate;

public abstract class RabbitNotificationService extends RabbitAbstractService<NotificationEvent> {

    public RabbitNotificationService(AmqpTemplate amqpTemplate, String exchangeName, String routingKey) {
        super(amqpTemplate, exchangeName, routingKey);
    }

    public abstract NotificationType getType();

    public boolean supports(NotificationType type) {
        return this.getType() == type || type == NotificationType.BOTH;
    }
}
