package com.jobflow.job_tracker_service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class JobTrackerServiceApplicationTest {

    @Test
    public void main_doNothing() {
        try (MockedStatic<SpringApplication> mockedStatic = mockStatic(SpringApplication.class)) {
            String[] args = {};
            JobTrackerServiceApplication.main(args);
            mockedStatic.verify(() -> SpringApplication.run(JobTrackerServiceApplication.class, args));
        }
    }
}