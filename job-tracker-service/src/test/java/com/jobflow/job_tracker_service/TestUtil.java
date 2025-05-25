package com.jobflow.job_tracker_service;

import com.jobflow.job_tracker_service.jobApplication.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class TestUtil {

    private TestUtil() {

    }

    public static <T> HttpEntity<T> createRequest(T request, HttpHeaders headers) {
        return new HttpEntity<>(request, headers);
    }

    public static <T> HttpEntity<T> createRequest(T request) {
        return createRequest(request, null);
    }

    public static JobApplicationCreateUpdateDto createJobApplicationCreateUpdateDto() {
        return JobApplicationCreateUpdateDto.builder()
                .company("Google")
                .position("Backend")
                .link("http://some-link")
                .source(Source.LINKEDIN)
                .salaryMin(100)
                .salaryMax(300)
                .currency(Currency.RUB)
                .status(Status.APPLIED)
                .comment("some-comment")
                .appliedAt(LocalDate.now())
                .build();
    }

    public static JobApplicationDto createJobApplicationDto() {
        return JobApplicationDto.builder()
                .id(1L)
                .userId(1L)
                .company("Google")
                .position("Backend")
                .link("http://some-link")
                .source(Source.LINKEDIN)
                .salaryMin(100)
                .salaryMax(300)
                .currency(Currency.RUB)
                .status(Status.APPLIED)
                .comment("some-comment")
                .appliedAt(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static JobApplication createJobApplication() {
        return JobApplication.builder()
                .id(1L)
                .userId(1L)
                .company("Google")
                .position("Backend")
                .link("http://some-link")
                .source(Source.LINKEDIN)
                .salaryMin(100)
                .salaryMax(300)
                .currency(Currency.RUB)
                .status(Status.APPLIED)
                .comment("some-comment")
                .appliedAt(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
