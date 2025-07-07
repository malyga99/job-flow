package com.jobflow.notification_service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class NotificationServiceApplicationTest {

    @Test
    public void main_doNothing() {
        try (MockedStatic<SpringApplication> mockedStatic = mockStatic(SpringApplication.class)) {
            String[] args = {};
            NotificationServiceApplication.main(args);
            mockedStatic.verify(() -> SpringApplication.run(NotificationServiceApplication.class, args));
        }
    }
}
