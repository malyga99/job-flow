package com.jobflow.job_tracker_service.rabbitMQ;

import org.springframework.amqp.core.AmqpTemplate;

public abstract class RabbitAbstractService<T> {

    protected final AmqpTemplate amqpTemplate;
    protected final String exchangeName;
    protected final String routingKey;

    public RabbitAbstractService(AmqpTemplate amqpTemplate, String exchangeName, String routingKey) {
        this.amqpTemplate = amqpTemplate;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
    }

    public abstract void send(T payload);
}
