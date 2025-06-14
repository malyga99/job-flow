package com.jobflow.job_tracker_service;

import com.jobflow.job_tracker_service.jobApplication.*;
import com.jobflow.job_tracker_service.jobApplication.stats.JobApplicationStatsDto;
import com.jobflow.job_tracker_service.jobApplication.stats.TopItem;
import com.jobflow.job_tracker_service.notification.NotificationEvent;
import com.jobflow.job_tracker_service.notification.NotificationType;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A utility class for use in unit/integration tests
 * and avoid repeatable code.
 */
public final class TestUtil {

    public static final Long USER_ID = 1L;

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

    public static JobApplicationStatsDto createStatsDto() {
        return JobApplicationStatsDto.builder()
                .total(1L)
                .last7Days(1L)
                .last30Days(1L)
                .uniqueCompanies(1L)
                .topCompany(new TopItem("Google", 1L))
                .topPosition(new TopItem("Backend", 1L))
                .byStatus(Map.of(Status.REJECTED, 1L, Status.OFFER, 1L, Status.APPLIED, 1L))
                .build();
    }

    public static NotificationEvent createNotificationEvent() {
        return NotificationEvent.builder()
                .userId(USER_ID)
                .notificationType(NotificationType.EMAIL)
                .subject("subject")
                .message("message")
                .build();
    }

    public static void clearDb(JpaRepository<?, ?> repository) {
        repository.deleteAll();
    }

    public static <T, ID> void saveDataInDb(JpaRepository<T, ID> repository, List<T> entities) {
        repository.saveAll(entities);
    }

    public static void clearRabbit(AmqpAdmin amqpAdmin, String queueName) {
        amqpAdmin.purgeQueue(queueName);
    }

    public static void clearKeys(RedisTemplate<String, String> redisTemplate, String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
