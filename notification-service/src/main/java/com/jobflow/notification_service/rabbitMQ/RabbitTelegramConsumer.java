package com.jobflow.notification_service.rabbitMQ;

import com.jobflow.notification_service.exception.NotificationException;
import com.jobflow.notification_service.notification.AbstractNotificationService;
import com.jobflow.notification_service.notification.NotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
public class RabbitTelegramConsumer extends RabbitNotificationConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitTelegramConsumer.class);

    @Value("${spring.rabbitmq.retryable-max-attempts}")
    private int maxAttempts;

    public RabbitTelegramConsumer(
            @Qualifier("telegramNotificationService") AbstractNotificationService<?> notificationService
    ) {
        super(notificationService);
    }

    @RabbitListener(queues = {"${spring.rabbitmq.telegram-queue-name}"})
    @Retryable(
            retryFor = NotificationException.class,
            maxAttemptsExpression = "${spring.rabbitmq.retryable-max-attempts}",
            recover = "recover",
            backoff = @Backoff(
                    delayExpression = "${spring.rabbitmq.retryable-delay}"
            )

    )
    @Override
    public void consume(NotificationEvent notificationEvent) {
        LOGGER.debug("Consuming notification event from Telegram queue: {}", notificationEvent);

        notificationService.send(notificationEvent);
    }

    @Recover
    public void recover(NotificationException exc, NotificationEvent payload) {
        LOGGER.debug("Failed to sending notification event: {} after {} retries. Sending to telegram DLQ. Reason: {}",
                payload, maxAttempts, exc.getMessage());

        throw exc;
    }
}
