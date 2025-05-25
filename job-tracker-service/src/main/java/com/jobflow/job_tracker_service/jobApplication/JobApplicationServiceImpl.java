package com.jobflow.job_tracker_service.jobApplication;

import com.jobflow.job_tracker_service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JobApplicationServiceImpl implements JobApplicationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobApplicationServiceImpl.class);

    private final UserService userService;
    private final JobApplicationMapper jobApplicationMapper;
    private final JobApplicationRepository jobApplicationRepository;

    @Override
    public JobApplicationDto create(JobApplicationCreateUpdateDto dto) {
        Long currentUserId = userService.getCurrentUserId();
        LOGGER.debug("Creating a new job application for userId: {}", currentUserId);

        JobApplication jobApplication = jobApplicationMapper.toEntity(dto, currentUserId);
        JobApplication savedJobApplication = jobApplicationRepository.save(jobApplication);

        LOGGER.debug("Successfully created job application with id: {} for userId: {}", savedJobApplication.getId(), currentUserId);
        return jobApplicationMapper.toDto(savedJobApplication);
    }
}
