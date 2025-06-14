package com.jobflow.job_tracker_service.jobApplication;

import com.jobflow.job_tracker_service.exception.JobApplicationNotFoundException;
import com.jobflow.job_tracker_service.exception.UserDontHavePermissionException;
import com.jobflow.job_tracker_service.jobApplication.stats.StatsCacheKeyUtils;
import com.jobflow.job_tracker_service.notification.EventPublisher;
import com.jobflow.job_tracker_service.notification.NotificationEvent;
import com.jobflow.job_tracker_service.notification.NotificationEventFactory;
import com.jobflow.job_tracker_service.rateLimiter.RateLimiterKeyUtil;
import com.jobflow.job_tracker_service.rateLimiter.RateLimiterService;
import com.jobflow.job_tracker_service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class JobApplicationServiceImpl implements JobApplicationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobApplicationServiceImpl.class);

    private final UserService userService;
    private final JobApplicationMapper jobApplicationMapper;
    private final JobApplicationRepository jobApplicationRepository;
    private final EventPublisher<NotificationEvent> eventPublisher;
    private final NotificationEventFactory eventFactory;
    private final RedisTemplate<String, String> redisTemplate;
    private final RateLimiterService rateLimiterService;

    @Override
    public Page<JobApplicationDto> findMy(Pageable pageable) {
        Long currentUserId = userService.getCurrentUserId();
        LOGGER.debug("Fetching job applications of the current user with id: {}", currentUserId);

        Page<JobApplication> jobApplications = jobApplicationRepository.findByUserId(currentUserId, pageable);

        LOGGER.debug("Fetched: {} job applications of the current user with id: {}", jobApplications.getContent().size(), currentUserId);
        return jobApplications.map(jobApplicationMapper::toDto);
    }

    @Override
    public JobApplicationDto findById(Long id) {
        Long currentUserId = userService.getCurrentUserId();
        LOGGER.debug("Fetching job application by id: {} for userId: {}", id, currentUserId);

        JobApplication jobApplication = findByIdOrThrow(id);
        checkUserPermissions(currentUserId, jobApplication);

        LOGGER.debug("Fetched job application by id: {} for userId: {}", id, currentUserId);
        return jobApplicationMapper.toDto(jobApplication);
    }

    @Override
    public JobApplicationDto create(JobApplicationCreateUpdateDto dto) {
        Long currentUserId = userService.getCurrentUserId();
        LOGGER.debug("Creating a new job application by userId: {}", currentUserId);

        rateLimiterService.validateOrThrow(
                RateLimiterKeyUtil.generateKey("create", String.valueOf(currentUserId)),
                5,
                Duration.ofMinutes(1),
                "Too many job applications created. Try again in a minute"
        );

        JobApplication jobApplication = jobApplicationMapper.toEntity(dto, currentUserId);
        JobApplication savedJobApplication = jobApplicationRepository.save(jobApplication);

        deleteFromCache(StatsCacheKeyUtils.keyForUser(currentUserId));
        eventPublisher.publish(
                eventFactory.buildForCreation(savedJobApplication)
        );

        LOGGER.debug("Successfully created job application with id: {} by userId: {}", savedJobApplication.getId(), currentUserId);
        return jobApplicationMapper.toDto(savedJobApplication);
    }

    @Override
    public void update(Long id, JobApplicationCreateUpdateDto dto) {
        Long currentUserId = userService.getCurrentUserId();
        LOGGER.debug("Updating job application with id: {} by userId: {}", id, currentUserId);

        rateLimiterService.validateOrThrow(
                RateLimiterKeyUtil.generateKey("update", String.valueOf(currentUserId)),
                10,
                Duration.ofMinutes(1),
                "Too many updates. Try again in a minute"
        );

        JobApplication jobApplication = findByIdOrThrow(id);
        checkUserPermissions(currentUserId, jobApplication);

        updateFields(jobApplication, dto);
        jobApplicationRepository.save(jobApplication);

        deleteFromCache(StatsCacheKeyUtils.keyForUser(currentUserId));
        eventPublisher.publish(
                eventFactory.buildForStatusUpdate(jobApplication)
        );

        LOGGER.debug("Successfully updated job application with id: {} by userId: {}", id, currentUserId);
    }

    @Override
    public void updateStatus(Long id, Status status) {
        Long currentUserId = userService.getCurrentUserId();
        LOGGER.debug("Updating the job application status with id: {} by userId: {}", id, currentUserId);

        rateLimiterService.validateOrThrow(
                RateLimiterKeyUtil.generateKey("updateStatus", String.valueOf(currentUserId)),
                10,
                Duration.ofMinutes(1),
                "Too many status updates. Try again in a minute"
        );

        JobApplication jobApplication = findByIdOrThrow(id);
        checkUserPermissions(currentUserId, jobApplication);

        jobApplication.setStatus(status);
        jobApplicationRepository.save(jobApplication);

        deleteFromCache(StatsCacheKeyUtils.keyForUser(currentUserId));
        eventPublisher.publish(
                eventFactory.buildForStatusUpdate(jobApplication)
        );

        LOGGER.debug("Successfully updated the job application status with id: {} by userId: {}", id, currentUserId);
    }

    @Override
    public void delete(Long id) {
        Long currentUserId = userService.getCurrentUserId();
        LOGGER.debug("Deleting the job application with id: {} by userId: {}", id, userService);

        rateLimiterService.validateOrThrow(
                RateLimiterKeyUtil.generateKey("delete", String.valueOf(currentUserId)),
                3,
                Duration.ofMinutes(1),
                "Too many delete attempts. Try again in a minute"
        );

        JobApplication jobApplication = findByIdOrThrow(id);
        checkUserPermissions(currentUserId, jobApplication);

        jobApplicationRepository.delete(jobApplication);

        deleteFromCache(StatsCacheKeyUtils.keyForUser(currentUserId));
        LOGGER.debug("Successfully deleted the job application with id: {} by userId: {}", id, userService);
    }

    private JobApplication findByIdOrThrow(Long id) {
        return jobApplicationRepository.findById(id)
                .orElseThrow(() -> new JobApplicationNotFoundException("Job application with id: " + id + " not found"));
    }

    private void deleteFromCache(String cacheKey) {
        redisTemplate.delete(cacheKey);
    }

    private void updateFields(JobApplication jobApplication, JobApplicationCreateUpdateDto dto) {
        jobApplication.setCompany(dto.getCompany());
        jobApplication.setPosition(dto.getPosition());
        jobApplication.setLink(dto.getLink());
        jobApplication.setSource(dto.getSource());
        jobApplication.setSourceDetails(dto.getSourceDetails());
        jobApplication.setSalaryMin(dto.getSalaryMin());
        jobApplication.setSalaryMax(dto.getSalaryMax());
        jobApplication.setCurrency(dto.getCurrency());
        jobApplication.setStatus(dto.getStatus());
        jobApplication.setComment(dto.getComment());
        jobApplication.setAppliedAt(dto.getAppliedAt());
    }

    private void checkUserPermissions(Long currentUserId, JobApplication jobApplication) {
        if (!currentUserId.equals(jobApplication.getUserId())) {
            throw new UserDontHavePermissionException("User with id: " + currentUserId + " cannot perform actions with job application with id: " + jobApplication.getId());
        }
    }
}
