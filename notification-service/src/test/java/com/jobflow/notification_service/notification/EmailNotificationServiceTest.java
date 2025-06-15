package com.jobflow.notification_service.notification;

import com.jobflow.notification_service.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EmailNotificationServiceTest {

    @InjectMocks
    private EmailNotificationService emailNotificationService;

    private NotificationEvent notificationEvent;

    @BeforeEach
    public void setup() {
        notificationEvent = TestUtil.createNotificationEvent();
    }

    @Test
    public void send_doNothing() {
        emailNotificationService.send(notificationEvent);
    }

}