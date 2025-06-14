package com.jobflow.job_tracker_service.jobApplication;

import com.jobflow.job_tracker_service.TestUtil;
import com.jobflow.job_tracker_service.exception.JobApplicationNotFoundException;
import com.jobflow.job_tracker_service.exception.UserDontHavePermissionException;
import com.jobflow.job_tracker_service.jobApplication.stats.StatsCacheKeyUtils;
import com.jobflow.job_tracker_service.notification.EventPublisher;
import com.jobflow.job_tracker_service.notification.NotificationEvent;
import com.jobflow.job_tracker_service.notification.NotificationEventFactory;
import com.jobflow.job_tracker_service.rateLimiter.RateLimiterKeyUtil;
import com.jobflow.job_tracker_service.rateLimiter.RateLimiterService;
import com.jobflow.job_tracker_service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobApplicationServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private JobApplicationMapper jobApplicationMapper;

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private NotificationEventFactory eventFactory;

    @Mock
    private EventPublisher<NotificationEvent> eventPublisher;

    @Mock
    private RateLimiterService rateLimiterService;

    @InjectMocks
    private JobApplicationServiceImpl jobApplicationService;

    private JobApplicationCreateUpdateDto createUpdateDto;

    private JobApplication firstJobApplication;

    private JobApplication secondJobApplication;

    private JobApplicationDto firstJobApplicationDto;

    private JobApplicationDto secondJobApplicationDto;

    private NotificationEvent notificationEvent;

    private Pageable mockPageable;

    private Page<JobApplication> mockPage;

    @BeforeEach
    public void setup() {
        createUpdateDto = TestUtil.createJobApplicationCreateUpdateDto();

        firstJobApplication = TestUtil.createJobApplication();
        secondJobApplication = TestUtil.createJobApplication();

        firstJobApplicationDto = TestUtil.createJobApplicationDto();
        secondJobApplicationDto = TestUtil.createJobApplicationDto();

        notificationEvent = TestUtil.createNotificationEvent();

        mockPageable = PageRequest.of(0, 10);
        mockPage = new PageImpl<>(List.of(firstJobApplication, secondJobApplication), mockPageable, 2);
    }

    @Test
    public void findMy_returnTwoJobApplications() {
        when(userService.getCurrentUserId()).thenReturn(1L);
        when(jobApplicationRepository.findByUserId(1L, mockPageable)).thenReturn(mockPage);
        when(jobApplicationMapper.toDto(firstJobApplication)).thenReturn(firstJobApplicationDto);
        when(jobApplicationMapper.toDto(secondJobApplication)).thenReturn(secondJobApplicationDto);

        Page<JobApplicationDto> result = jobApplicationService.findMy(mockPageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(firstJobApplicationDto, result.getContent().get(0));
        assertEquals(secondJobApplicationDto, result.getContent().get(1));

        verify(jobApplicationRepository, times(1)).findByUserId(1L, mockPageable);
        verify(jobApplicationMapper, times(2)).toDto(any(JobApplication.class));
    }

    @Test
    public void findById_returnJobApplication() {
        firstJobApplication.setUserId(1L);
        when(userService.getCurrentUserId()).thenReturn(1L);
        when(jobApplicationRepository.findById(1L)).thenReturn(Optional.of(firstJobApplication));
        when(jobApplicationMapper.toDto(firstJobApplication)).thenReturn(firstJobApplicationDto);

        JobApplicationDto result = jobApplicationService.findById(1L);

        assertNotNull(result);
        assertEquals(firstJobApplicationDto, result);

        verify(jobApplicationRepository, times(1)).findById(1L);
        verify(jobApplicationMapper, times(1)).toDto(firstJobApplication);
    }

    @Test
    public void findById_jobApplicationNotFound_throwExc() {
        when(userService.getCurrentUserId()).thenReturn(1L);
        when(jobApplicationRepository.findById(1L)).thenReturn(Optional.empty());

        var jobApplicationNotFoundException = assertThrows(JobApplicationNotFoundException.class, () -> jobApplicationService.findById(1L));
        assertEquals("Job application with id: 1 not found", jobApplicationNotFoundException.getMessage());
    }

    @Test
    public void findById_userDontHavePermissions_throwExc() {
        firstJobApplication.setUserId(999L);
        when(userService.getCurrentUserId()).thenReturn(1L);
        when(jobApplicationRepository.findById(1L)).thenReturn(Optional.of(firstJobApplication));

        var userDontHavePermissionException = assertThrows(UserDontHavePermissionException.class, () -> jobApplicationService.findById(1L));
        assertEquals("User with id: 1 cannot perform actions with job application with id: " + firstJobApplication.getId(),
                userDontHavePermissionException.getMessage());
    }

    @Test
    public void create_returnCreatedJobApplication() {
        when(userService.getCurrentUserId()).thenReturn(1L);
        when(jobApplicationMapper.toEntity(createUpdateDto, 1L)).thenReturn(firstJobApplication);
        when(jobApplicationRepository.save(firstJobApplication)).thenReturn(firstJobApplication);
        when(jobApplicationMapper.toDto(firstJobApplication)).thenReturn(firstJobApplicationDto);
        when(eventFactory.buildForCreation(firstJobApplication)).thenReturn(notificationEvent);

        JobApplicationDto result = jobApplicationService.create(createUpdateDto);

        assertNotNull(result);
        assertEquals(firstJobApplicationDto, result);

        verify(rateLimiterService, times(1)).validateOrThrow(
                RateLimiterKeyUtil.generateKey("create", "1"),
                5,
                Duration.ofMinutes(1),
                "Too many job applications created. Try again in a minute"
        );
        verify(eventPublisher, times(1)).publish(notificationEvent);
        verify(jobApplicationRepository, times(1)).save(firstJobApplication);
        verify(redisTemplate, times(1)).delete(StatsCacheKeyUtils.keyForUser(1L));
    }

    @Test
    public void update_updatesJobApplicationCorrectly() {
        var dataToUpdate = JobApplicationCreateUpdateDto.builder()
                .company("Updated company")
                .position("Updated position")
                .link("Updated link")
                .source(Source.LINKEDIN)
                .salaryMin(100)
                .salaryMax(300)
                .currency(Currency.RUB)
                .status(Status.APPLIED)
                .comment("Updated comment")
                .appliedAt(LocalDate.now())
                .build();
        var argumentCaptor = ArgumentCaptor.forClass(JobApplication.class);
        firstJobApplication.setUserId(1L);
        when(userService.getCurrentUserId()).thenReturn(1L);
        when(jobApplicationRepository.findById(1L)).thenReturn(Optional.of(firstJobApplication));
        when(eventFactory.buildForStatusUpdate(firstJobApplication)).thenReturn(notificationEvent);

        jobApplicationService.update(1L, dataToUpdate);

        verify(rateLimiterService, times(1)).validateOrThrow(
                RateLimiterKeyUtil.generateKey("update", "1"),
                10,
                Duration.ofMinutes(1),
                "Too many updates. Try again in a minute"
        );
        verify(eventPublisher, times(1)).publish(notificationEvent);
        verify(jobApplicationRepository, times(1)).findById(1L);
        verify(jobApplicationRepository, times(1)).save(argumentCaptor.capture());
        verify(redisTemplate, times(1)).delete(StatsCacheKeyUtils.keyForUser(1L));

        JobApplication jobApplication = argumentCaptor.getValue();
        assertEquals(dataToUpdate.getCompany(), jobApplication.getCompany());
        assertEquals(dataToUpdate.getPosition(), jobApplication.getPosition());
        assertEquals(dataToUpdate.getLink(), jobApplication.getLink());
        assertEquals(dataToUpdate.getSource(), jobApplication.getSource());
        assertEquals(dataToUpdate.getSalaryMin(), jobApplication.getSalaryMin());
        assertEquals(dataToUpdate.getSalaryMax(), jobApplication.getSalaryMax());
        assertEquals(dataToUpdate.getCurrency(), jobApplication.getCurrency());
        assertEquals(dataToUpdate.getStatus(), jobApplication.getStatus());
        assertEquals(dataToUpdate.getComment(), jobApplication.getComment());
        assertEquals(dataToUpdate.getAppliedAt(), jobApplication.getAppliedAt());
        assertNotNull(jobApplication.getId());
        assertNotNull(jobApplication.getUserId());
        assertNotNull(jobApplication.getCreatedAt());
        assertNotNull(jobApplication.getUpdatedAt());
    }

    @Test
    public void update_jobApplicationNotFound_throwExc() {
        when(userService.getCurrentUserId()).thenReturn(1L);
        when(jobApplicationRepository.findById(1L)).thenReturn(Optional.empty());

        var jobApplicationNotFoundException = assertThrows(JobApplicationNotFoundException.class, () -> jobApplicationService.update(1L, createUpdateDto));
        assertEquals("Job application with id: 1 not found", jobApplicationNotFoundException.getMessage());
    }

    @Test
    public void update_userDontHavePermissions_throwExc() {
        firstJobApplication.setUserId(999L);
        when(userService.getCurrentUserId()).thenReturn(1L);
        when(jobApplicationRepository.findById(1L)).thenReturn(Optional.of(firstJobApplication));

        var userDontHavePermissionException = assertThrows(UserDontHavePermissionException.class, () -> jobApplicationService.update(1L, createUpdateDto));
        assertEquals("User with id: 1 cannot perform actions with job application with id: " + firstJobApplication.getId(),
                userDontHavePermissionException.getMessage());
    }

    @Test
    public void updateStatus_updatesStatusCorrectly() {
        firstJobApplication.setUserId(1L);
        when(userService.getCurrentUserId()).thenReturn(1L);
        when(jobApplicationRepository.findById(1L)).thenReturn(Optional.of(firstJobApplication));
        when(eventFactory.buildForStatusUpdate(firstJobApplication)).thenReturn(notificationEvent);
        var argumentCaptor = ArgumentCaptor.forClass(JobApplication.class);

        jobApplicationService.updateStatus(1L, Status.REJECTED);

        verify(rateLimiterService, times(1)).validateOrThrow(
                RateLimiterKeyUtil.generateKey("updateStatus", "1"),
                10,
                Duration.ofMinutes(1),
                "Too many status updates. Try again in a minute"
        );
        verify(eventPublisher, times(1)).publish(notificationEvent);
        verify(jobApplicationRepository, times(1)).findById(1L);
        verify(jobApplicationRepository, times(1)).save(argumentCaptor.capture());

        JobApplication jobApplication = argumentCaptor.getValue();
        assertEquals(Status.REJECTED, jobApplication.getStatus());

        verify(redisTemplate, times(1)).delete(StatsCacheKeyUtils.keyForUser(1L));
    }

    @Test
    public void updateStatus_jobApplicationNotFound_throwExc() {
        when(userService.getCurrentUserId()).thenReturn(1L);
        when(jobApplicationRepository.findById(1L)).thenReturn(Optional.empty());

        var jobApplicationNotFoundException = assertThrows(JobApplicationNotFoundException.class, () -> jobApplicationService.updateStatus(1L, Status.REJECTED));
        assertEquals("Job application with id: 1 not found", jobApplicationNotFoundException.getMessage());
    }

    @Test
    public void updateStatus_userDontHavePermissions_throwExc() {
        firstJobApplication.setUserId(999L);
        when(userService.getCurrentUserId()).thenReturn(1L);
        when(jobApplicationRepository.findById(1L)).thenReturn(Optional.of(firstJobApplication));

        var userDontHavePermissionException = assertThrows(UserDontHavePermissionException.class, () -> jobApplicationService.updateStatus(1L, Status.REJECTED));
        assertEquals("User with id: 1 cannot perform actions with job application with id: " + firstJobApplication.getId(),
                userDontHavePermissionException.getMessage());
    }

    @Test
    public void delete_deletesJobApplication() {
        firstJobApplication.setUserId(1L);
        when(userService.getCurrentUserId()).thenReturn(1L);
        when(jobApplicationRepository.findById(1L)).thenReturn(Optional.of(firstJobApplication));

        jobApplicationService.delete(1L);

        verify(rateLimiterService, times(1)).validateOrThrow(
                RateLimiterKeyUtil.generateKey("delete", "1"),
                3,
                Duration.ofMinutes(1),
                "Too many delete attempts. Try again in a minute"
        );
        verify(jobApplicationRepository, times(1)).findById(1L);
        verify(jobApplicationRepository, times(1)).delete(firstJobApplication);
        verify(redisTemplate, times(1)).delete(StatsCacheKeyUtils.keyForUser(1L));
    }

    @Test
    public void delete_jobApplicationNotFound_throwExc() {
        when(userService.getCurrentUserId()).thenReturn(1L);
        when(jobApplicationRepository.findById(1L)).thenReturn(Optional.empty());

        var jobApplicationNotFoundException = assertThrows(JobApplicationNotFoundException.class, () -> jobApplicationService.delete(1L));
        assertEquals("Job application with id: 1 not found", jobApplicationNotFoundException.getMessage());
    }

    @Test
    public void delete_userDontHavePermissions_throwExc() {
        firstJobApplication.setUserId(999L);
        when(userService.getCurrentUserId()).thenReturn(1L);
        when(jobApplicationRepository.findById(1L)).thenReturn(Optional.of(firstJobApplication));

        var userDontHavePermissionException = assertThrows(UserDontHavePermissionException.class, () -> jobApplicationService.delete(1L));
        assertEquals("User with id: 1 cannot perform actions with job application with id: " + firstJobApplication.getId(),
                userDontHavePermissionException.getMessage());
    }
}