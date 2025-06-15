package com.jobflow.notification_service.rabbitMQ;

public abstract class RabbitAbstractService<T> {

    public abstract void consume(T payload);
}
