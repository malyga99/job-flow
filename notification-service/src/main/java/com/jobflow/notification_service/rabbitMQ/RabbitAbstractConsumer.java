package com.jobflow.notification_service.rabbitMQ;

public abstract class RabbitAbstractConsumer<T> {

    public abstract void consume(T payload);
}
