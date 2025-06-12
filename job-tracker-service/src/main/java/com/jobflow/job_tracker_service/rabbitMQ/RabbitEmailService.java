package com.jobflow.job_tracker_service.rabbitMQ;

import com.jobflow.job_tracker_service.notification.NotificationEvent;
import com.jobflow.job_tracker_service.notification.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;

@Service
public class RabbitEmailService extends RabbitNotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitEmailService.class);

    public RabbitEmailService(AmqpTemplate amqpTemplate, RabbitProperties rabbitProperties) {
        super(amqpTemplate, rabbitProperties.getExchangeName(), rabbitProperties.getEmailQueueRoutingKey());
    }

    @Override
    public void send(NotificationEvent payload) {
        LOGGER.debug("Sending Email notification event to exchange: {} with routing key: {} for userId: {}",
                exchangeName, routingKey, payload.getUserId());

        amqpTemplate.convertAndSend(exchangeName, routingKey, payload);

        LOGGER.debug("Successfully sent Email notification event to exchange: {} with routing key: {} for userId: {}",
                exchangeName, routingKey, payload.getUserId());
    }

    @Override
    public NotificationType getType() {
        return NotificationType.EMAIL;
    }
}
