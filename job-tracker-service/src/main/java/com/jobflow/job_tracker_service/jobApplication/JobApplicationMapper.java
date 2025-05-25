package com.jobflow.job_tracker_service.jobApplication;

import org.springframework.stereotype.Component;

@Component
public class JobApplicationMapper {

    public JobApplication toEntity(JobApplicationCreateUpdateDto dto, Long userId) {
        return JobApplication.builder()
                .userId(userId)
                .company(dto.getCompany())
                .position(dto.getPosition())
                .link(dto.getLink())
                .source(dto.getSource())
                .sourceDetails(dto.getSourceDetails())
                .salaryMin(dto.getSalaryMin())
                .salaryMax(dto.getSalaryMax())
                .currency(dto.getCurrency())
                .status(dto.getStatus())
                .comment(dto.getComment())
                .appliedAt(dto.getAppliedAt())
                .build();
    }

    public JobApplicationDto toDto(JobApplication jobApplication) {
        return JobApplicationDto.builder()
                .id(jobApplication.getId())
                .userId(jobApplication.getUserId())
                .company(jobApplication.getCompany())
                .position(jobApplication.getPosition())
                .link(jobApplication.getLink())
                .source(jobApplication.getSource())
                .sourceDetails(jobApplication.getSourceDetails())
                .salaryMin(jobApplication.getSalaryMin())
                .salaryMax(jobApplication.getSalaryMax())
                .currency(jobApplication.getCurrency())
                .status(jobApplication.getStatus())
                .comment(jobApplication.getComment())
                .appliedAt(jobApplication.getAppliedAt())
                .createdAt(jobApplication.getCreatedAt())
                .updatedAt(jobApplication.getUpdatedAt())
                .build();
    }
}
