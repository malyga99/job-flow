package com.jobflow.user_service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceApplicationTest {

    @Test
    public void main_doNothing() {
        try (MockedStatic<SpringApplication> mockedStatic = mockStatic(SpringApplication.class)) {
            String[] args = {};
            UserServiceApplication.main(args);
            mockedStatic.verify(() -> SpringApplication.run(UserServiceApplication.class, args));
        }
    }
}