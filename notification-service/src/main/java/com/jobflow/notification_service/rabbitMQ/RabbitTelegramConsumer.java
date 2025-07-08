package com.jobflow.notification_service.rabbitMQ;

import com.jobflow.notification_service.notification.AbstractNotificationService;
import com.jobflow.notification_service.notification.NotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class RabbitTelegramConsumer extends RabbitNotificationConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitTelegramConsumer.class);

    public RabbitTelegramConsumer(
            @Qualifier("telegramNotificationService") AbstractNotificationService<?> notificationService
    ) {
        super(notificationService);
    }

    @RabbitListener(queues = {"${spring.rabbitmq.telegram-queue-name}"})
    @Override
    public void consume(NotificationEvent notificationEvent) {
        LOGGER.debug("Consuming notification event from Telegram queue: {}", notificationEvent);

        notificationService.send(notificationEvent);
    }
}
