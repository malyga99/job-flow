package com.jobflow.job_tracker_service.jobApplication;

import com.jobflow.job_tracker_service.TestUtil;
import com.jobflow.job_tracker_service.exception.JobApplicationNotFoundException;
import com.jobflow.job_tracker_service.exception.UserDontHavePermissionException;
import com.jobflow.job_tracker_service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

    @InjectMocks
    private JobApplicationServiceImpl jobApplicationService;

    private JobApplicationCreateUpdateDto createUpdateDto;

    private JobApplication firstJobApplication;

    private JobApplication secondJobApplication;

    private JobApplicationDto firstJobApplicationDto;

    private JobApplicationDto secondJobApplicationDto;

    private Pageable mockPageable;

    private Page<JobApplication> mockPage;

    @BeforeEach
    public void setup() {
        createUpdateDto = TestUtil.createJobApplicationCreateUpdateDto();

        firstJobApplication = TestUtil.createJobApplication();
        secondJobApplication = TestUtil.createJobApplication();

        firstJobApplicationDto = TestUtil.createJobApplicationDto();
        secondJobApplicationDto = TestUtil.createJobApplicationDto();

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
        firstJobApplication.setUserId(3L);
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

        JobApplicationDto result = jobApplicationService.create(createUpdateDto);

        assertNotNull(result);
        assertEquals(firstJobApplicationDto, result);

        verify(jobApplicationRepository, times(1)).save(firstJobApplication);
    }
}