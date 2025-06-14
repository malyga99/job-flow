package com.jobflow.job_tracker_service.rabbitMQ;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.rabbitmq")
@Getter
@Setter
public class RabbitProperties {

    private String host;

    private String port;

    private String username;

    private String password;

    private String exchangeName;

    private String emailQueueName;

    private String telegramQueueName;

    private String emailQueueRoutingKey;

    private String telegramQueueRoutingKey;
}
