package com.jobflow.job_tracker_service.notification;

import com.jobflow.job_tracker_service.rabbitMQ.RabbitNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

@Service
public class NotificationEventPublisher implements EventPublisher<NotificationEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationEventPublisher.class);

    private final Map<NotificationType, RabbitNotificationService> notificationServices;

    public NotificationEventPublisher(List<RabbitNotificationService> notificationServices){
        this.notificationServices = notificationServices.stream()
                .collect(toMap(RabbitNotificationService::getType, Function.identity()));
    }

    @Override
    public void publish(NotificationEvent event) {
        NotificationType notificationType = event.getNotificationType();

        LOGGER.debug("Publishing notification event of type: {} for userId: {}", notificationType, event.getUserId());

        notificationServices.values().stream()
                .filter(el -> el.supports(notificationType))
                .forEach(el -> {
                    LOGGER.debug("Delegating event publishing to: {}", el.getClass().getSimpleName());
                    el.send(event);
                });
    }
}

