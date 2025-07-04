package com.jobflow.job_tracker_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class JobTrackerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobTrackerServiceApplication.class, args);
    }

}
