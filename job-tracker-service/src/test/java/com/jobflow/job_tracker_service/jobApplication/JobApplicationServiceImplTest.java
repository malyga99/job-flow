package com.jobflow.job_tracker_service.jobApplication;

import com.jobflow.job_tracker_service.TestUtil;
import com.jobflow.job_tracker_service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

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

    private JobApplication jobApplication;

    private JobApplicationDto jobApplicationDto;

    @BeforeEach
    public void setup() {
        createUpdateDto = TestUtil.createJobApplicationCreateUpdateDto();
        jobApplication = TestUtil.createJobApplication();
        jobApplicationDto = TestUtil.createJobApplicationDto();
    }

    @Test
    public void create_returnCreatedJobApplication() {
        when(userService.getCurrentUserId()).thenReturn(1L);
        when(jobApplicationMapper.toEntity(createUpdateDto, 1L)).thenReturn(jobApplication);
        when(jobApplicationRepository.save(jobApplication)).thenReturn(jobApplication);
        when(jobApplicationMapper.toDto(jobApplication)).thenReturn(jobApplicationDto);

        JobApplicationDto result = jobApplicationService.create(createUpdateDto);

        assertNotNull(result);
        assertEquals(jobApplicationDto, result);

        verify(jobApplicationRepository, times(1)).save(jobApplication);
    }
}