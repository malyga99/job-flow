package com.jobflow.job_tracker_service.jobApplication.stats;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobflow.job_tracker_service.exception.JobApplicationServiceException;
import com.jobflow.job_tracker_service.jobApplication.JobApplicationRateLimiterAction;
import com.jobflow.job_tracker_service.jobApplication.Status;
import com.jobflow.job_tracker_service.rateLimiter.RateLimiterValidator;
import com.jobflow.job_tracker_service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
public class JobApplicationStatsServiceImpl implements JobApplicationStatsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobApplicationStatsServiceImpl.class);

    private final JobApplicationStatsRepository statsRepository;
    private final UserService userService;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final RateLimiterValidator rateLimiterValidator;

    @Override
    public JobApplicationStatsDto getStats() {
        Long currentUserId = userService.getCurrentUserId();
        LOGGER.debug("Fetching job applications stats of the current user with id: {}", currentUserId);

        rateLimiterValidator.validate(JobApplicationStatsRateLimiterAction.GET_STATS, String.valueOf(currentUserId));
        String cacheKey = StatsCacheKeyUtils.keyForUser(currentUserId);

        String statsFromCache = redisTemplate.opsForValue().get(cacheKey);
        if (statsFromCache != null) {
            try {
                LOGGER.debug("Successfully extracted job applications stats from Redis for userId: {}", currentUserId);
                return objectMapper.readValue(statsFromCache, JobApplicationStatsDto.class);
            } catch (JsonProcessingException e) {
                throw new JobApplicationServiceException("Failed to deserialize job application stats", e);
            }
        }

        JobApplicationStatsDto stats = buildStats(currentUserId);

        try {
            String jobApplicationStatsJson = objectMapper.writeValueAsString(stats);
            redisTemplate.opsForValue().set(cacheKey, jobApplicationStatsJson, Duration.ofHours(1L));

            LOGGER.debug("Successfully fetched job applications stats and stored in Redis for userId: {}", currentUserId);
        } catch (JsonProcessingException e) {
            throw new JobApplicationServiceException("Failed to serialize job application stats", e);
        }

        return stats;
    }

    private JobApplicationStatsDto buildStats(Long userId) {
        Long total = statsRepository.countAllByUserId(userId);
        Long last7Days = statsRepository.countSince(userId, LocalDate.now().minusDays(7L));
        Long last30Days = statsRepository.countSince(userId, LocalDate.now().minusDays(30L));
        Long uniqueCompanies = statsRepository.countUniqueCompanies(userId);

        TopItem topCompany = statsRepository.findTopCompany(userId)
                .map(el -> new TopItem(el.getCompany(), el.getTotal()))
                .orElse(null);

        TopItem topPosition = statsRepository.findTopPosition(userId)
                .map(el -> new TopItem(el.getPosition(), el.getTotal()))
                .orElse(null);

        Map<Status, Long> byStatus = statsRepository.countByStatus(userId).stream()
                .collect(toMap(StatusCountProjection::getStatus, StatusCountProjection::getTotal));

        return JobApplicationStatsDto.builder()
                .total(total)
                .last7Days(last7Days)
                .last30Days(last30Days)
                .uniqueCompanies(uniqueCompanies)
                .topCompany(topCompany)
                .topPosition(topPosition)
                .byStatus(byStatus)
                .build();
    }

}
