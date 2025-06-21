package com.jobflow.notification_service.email;

public interface EmailService {
    void send(String email, String subject, String text);
}
