package com.jobflow.job_tracker_service.notification;

public interface EventPublisher<T> {

    void publish(T event);
}
