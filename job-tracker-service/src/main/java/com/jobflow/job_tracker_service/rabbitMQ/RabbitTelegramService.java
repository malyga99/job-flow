package com.jobflow.job_tracker_service.rabbitMQ;

import com.jobflow.job_tracker_service.notification.NotificationEvent;
import com.jobflow.job_tracker_service.notification.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;

@Service
public class RabbitTelegramService extends RabbitNotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitTelegramService.class);

    public RabbitTelegramService(AmqpTemplate amqpTemplate, RabbitProperties rabbitProperties) {
        super(amqpTemplate, rabbitProperties.getExchangeName(), rabbitProperties.getTelegramQueueRoutingKey());
    }

    @Override
    public void send(NotificationEvent payload) {
        LOGGER.debug("Sending Telegram notification event to exchange: {} with routing key: {} for userId: {}",
                exchangeName, routingKey, payload.getUserId());

        amqpTemplate.convertAndSend(exchangeName, routingKey, payload);

        LOGGER.debug("Successfully sent Telegram notification event to exchange: {} with routing key: {} for userId: {}",
                exchangeName, routingKey, payload.getUserId());
    }

    @Override
    public NotificationType getType() {
        return NotificationType.TELEGRAM;
    }
}
