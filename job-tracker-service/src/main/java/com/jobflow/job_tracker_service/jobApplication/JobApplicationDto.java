package com.jobflow.job_tracker_service.jobApplication;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO representing a job application's details")
public class JobApplicationDto {

    @Schema(description = "Job application id", example = "1")
    private Long id;

    @Schema(description = "User id. Owner of the job application", example = "1")
    private Long userId;

    @Schema(description = "Company in a job application", example = "Google")
    private String company;

    @Schema(description = "Position in a job application", example = "Backend")
    private String position;

    @Schema(description = "Link to the job application", example = "http://some_link")
    private String link;

    @Schema(description = "Source of the job application", example = "LINKEDIN")
    private Source source;

    @Schema(description = "Source details of the job application. Specified if source = 'OTHER'", example = "some_details")
    private String sourceDetails;

    @Schema(description = "Minimum salary in a job application", example = "3000")
    private Integer salaryMin;

    @Schema(description = "Maximum salary in a job application", example = "6000")
    private Integer salaryMax;

    @Schema(description = "Currency in a job application", example = "RUB")
    private Currency currency;

    @Schema(description = "Job application status", example = "APPLIED")
    private Status status;

    @Schema(description = "Comment on the job application", example = "some_comment")
    private String comment;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Date when the user applied for the job application", example = "2023-01-01")
    private LocalDate appliedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @Schema(description = "Job application creation date", example = "2023-01-01 12:00")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @Schema(description = "Job application update date", example = "2023-01-01 12:00")
    private LocalDateTime updatedAt;
}
