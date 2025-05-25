package com.jobflow.job_tracker_service.jobApplication;

import com.jobflow.job_tracker_service.handler.ResponseError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/job-applications")
@RequiredArgsConstructor
public class JobApplicationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobApplicationController.class);

    private final JobApplicationService jobApplicationService;

    @Operation(
            summary = "Find all job applications of the current user",
            description = "Searches all job applications of the current user with pagination and sorting",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Job applications successfully received"),

                    @ApiResponse(responseCode = "401", description = "Authentication exception, e.g incorrect sort field or direction",
                            content = @Content(mediaType = "application/json", schema =
                            @Schema(implementation = ResponseError.class)))
            }
    )
    @GetMapping("/my")
    public ResponseEntity<Page<JobApplicationDto>> findMy(
            @RequestParam(value = "pageNumber", defaultValue = "0") @Parameter(description = "Page number", example = "1") int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10") @Parameter(description = "Page size", example = "10") int pageSize,
            @RequestParam(value = "sortBy", defaultValue = "UPDATED_AT") @Parameter(description = "Sorting field", example = "UPDATED_AT") SortField sortBy,
            @RequestParam(value = "direction", defaultValue = "DESC") @Parameter(description = "Sorting direction", example = "DESC") Direction direction
    ) {
        LOGGER.info("[GET] Request for find all job applications of the current user - pageNumber: {}, pageSize: {}, sortBy: {}, direction: {}", pageNumber, pageSize, sortBy, direction);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortBy.getField()));

        return ResponseEntity.ok(jobApplicationService.findMy(pageable));
    }

    @Operation(
            summary = "Find job application by ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Job application successfully received",
                            content = @Content(mediaType = "application/json", schema =
                            @Schema(implementation = JobApplicationDto.class))),

                    @ApiResponse(responseCode = "404", description = "Job application not found",
                            content = @Content(mediaType = "application/json", schema =
                            @Schema(implementation = ResponseError.class))),

                    @ApiResponse(responseCode = "401", description = "Authentication exception, e.g user is trying to get a job application that is not his own",
                            content = @Content(mediaType = "application/json", schema =
                            @Schema(implementation = ResponseError.class)))
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<JobApplicationDto> findById(
            @PathVariable("id") @Parameter(description = "Job application ID", example = "1", required = true) Long id
    ) {
        LOGGER.info("[GET] Request for find job application by id: {}", id);

        return ResponseEntity.ok(jobApplicationService.findById(id));
    }

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
