package com.jobflow.notification_service.rabbitMQ;

import com.jobflow.notification_service.notification.NotificationEvent;
import com.jobflow.notification_service.notification.NotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class RabbitEmailService extends RabbitNotificationService {

    public RabbitEmailService(NotificationService notificationService) {
        super(notificationService);
    }

    @RabbitListener(queues = {"${spring.rabbitmq.email-queue-name}"})
    @Override
    public void consume(NotificationEvent notificationEvent) {
        notificationService.send(notificationEvent);
    }
}
