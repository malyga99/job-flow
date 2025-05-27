package com.jobflow.job_tracker_service.jobApplication;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface JobApplicationService {

    JobApplicationDto create(JobApplicationCreateUpdateDto dto);

    Page<JobApplicationDto> findMy(Pageable pageable);

    JobApplicationDto findById(Long id);

    void update(Long id, JobApplicationCreateUpdateDto dto);

    void updateStatus(Long id, Status status);
}
