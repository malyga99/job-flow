package com.jobflow.job_tracker_service.jobApplication;

import com.jobflow.job_tracker_service.handler.ResponseError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/job-applications")
@RequiredArgsConstructor
public class JobApplicationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobApplicationController.class);

    private final JobApplicationService jobApplicationService;

    @Operation(
            summary = "Job application creation",
            description = "Creates a new job application with the provided data",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Job application created successfully",
                            content = @Content(mediaType = "application/json", schema =
                            @Schema(implementation = JobApplicationDto.class))),

                    @ApiResponse(responseCode = "400", description = "Validation error",
                            content = @Content(mediaType = "application/json", schema =
                            @Schema(implementation = ResponseError.class))),

                    @ApiResponse(responseCode = "401", description = "Authentication exception",
                            content = @Content(mediaType = "application/json", schema =
                            @Schema(implementation = ResponseError.class)))
            }
    )
    @PostMapping
    public ResponseEntity<JobApplicationDto> create(
            @RequestBody @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Job application details", required = true
            ) JobApplicationCreateUpdateDto dto
    ) {
        LOGGER.info("[POST] Create job application request received");
        JobApplicationDto createdDto = jobApplicationService.create(dto);

        return ResponseEntity.created(URI.create("/api/v1/job-applications/" + createdDto.getId()))
                .body(createdDto);
    }
}
