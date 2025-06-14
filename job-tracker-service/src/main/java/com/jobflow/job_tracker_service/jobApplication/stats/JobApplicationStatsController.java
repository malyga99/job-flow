package com.jobflow.job_tracker_service.jobApplication.stats;

import com.jobflow.job_tracker_service.handler.ResponseError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/job-applications/stats")
@RequiredArgsConstructor
public class JobApplicationStatsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobApplicationStatsController.class);

    private final JobApplicationStatsService statsService;

    @Operation(
            summary = "Get stats of the current user",
            description = "Retrieves job application statistics for current user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Statistics received successfully",
                            content = @Content(mediaType = "application/json", schema =
                            @Schema(implementation = JobApplicationStatsDto.class))),

                    @ApiResponse(responseCode = "429", description = "Too many requests",
                            content = @Content(mediaType = "application/json", schema =
                            @Schema(implementation = ResponseError.class))),

                    @ApiResponse(responseCode = "401", description = "Authentication exception",
                            content = @Content(mediaType = "application/json", schema =
                            @Schema(implementation = ResponseError.class)))
            }
    )
    @GetMapping
    public ResponseEntity<JobApplicationStatsDto> getStats() {
        LOGGER.info("[GET] Request for get job application stats");

        return ResponseEntity.ok(statsService.getStats());
    }
}
