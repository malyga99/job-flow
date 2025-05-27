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

    public JobApplicationDto toDto(JobApplication entity) {
        return JobApplicationDto.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .company(entity.getCompany())
                .position(entity.getPosition())
                .link(entity.getLink())
                .source(entity.getSource())
                .sourceDetails(entity.getSourceDetails())
                .salaryMin(entity.getSalaryMin())
                .salaryMax(entity.getSalaryMax())
                .currency(entity.getCurrency())
                .status(entity.getStatus())
                .comment(entity.getComment())
                .appliedAt(entity.getAppliedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
