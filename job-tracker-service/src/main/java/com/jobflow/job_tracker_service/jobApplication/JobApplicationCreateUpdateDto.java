package com.jobflow.job_tracker_service.jobApplication;

import com.jobflow.job_tracker_service.validation.ValidSalary;
import com.jobflow.job_tracker_service.validation.ValidSourceDetails;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidSourceDetails
@ValidSalary
@Schema(description = "DTO representing a request to create/update a job application")
public class JobApplicationCreateUpdateDto {

    @NotBlank(message = "Company must be filled in!")
    @Size(max = 200, message = "Maximum company length is 200 characters!")
    @Schema(description = "Company in a job application", example = "Google")
    private String company;

    @NotBlank(message = "Position must be filled in!")
    @Size(max = 100, message = "Maximum position length is 100 characters!")
    @Schema(description = "Position in a job application", example = "Backend")
    private String position;

    @Size(max = 500, message = "Maximum link length is 500 characters!")
    @Schema(description = "Link to the job application", example = "http://some_link", requiredMode = NOT_REQUIRED)
    private String link;

    @NotNull(message = "Source must be filled in!")
    @Schema(description = "Source of the job application", example = "LINKEDIN")
    private Source source;

    @Size(max = 100, message = "Maximum source details length is 100 characters!")
    @Schema(description = "Source details of the job application. Specified if source = 'OTHER'", example = "some_details")
    private String sourceDetails;

    @Min(value = 0L, message = "Minimum salary should be more than 0!")
    @Schema(description = "Minimum salary in a job application", example = "3000", requiredMode = NOT_REQUIRED)
    private Integer salaryMin;

    @Min(value = 0L, message = "Maximum salary should be more than 0!")
    @Schema(description = "Maximum salary in a job application", example = "6000", requiredMode = NOT_REQUIRED)
    private Integer salaryMax;

    @Schema(description = "Currency in a job application", example = "RUB", requiredMode = NOT_REQUIRED)
    private Currency currency;

    @NotNull(message = "Status must be filled in!")
    @Schema(description = "Job application status", example = "APPLIED")
    private Status status;

    @Size(max = 500, message = "Maximum comment length is 500 characters!")
    @Schema(description = "Comment on the job application", example = "some_comment", requiredMode = NOT_REQUIRED)
    private String comment;

    @NotNull(message = "Applied at must be filled in!")
    @Schema(description = "Date when the user applied for the job application", example = "2023-01-01")
    private LocalDate appliedAt;
}
