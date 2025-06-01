package com.jobflow.job_tracker_service.jobApplication.stats;

import com.jobflow.job_tracker_service.jobApplication.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO representing a job application's statistics")
public class JobApplicationStatsDto {

    @Schema(description = "Total number of job applications", example = "1")
    private Long total;

    @Schema(description = "Number of job applications submitted in the last 7 days", example = "1")
    private Long last7Days;

    @Schema(description = "Number of job applications submitted in the last 30 days", example = "1")
    private Long last30Days;

    @Schema(description = "Number of unique companies in job applications", example = "1")
    private Long uniqueCompanies;

    @Schema(description = "Most frequently mentioned company in job applications, with total count", implementation = TopItem.class)
    private TopItem topCompany;

    @Schema(description = "Most frequently mentioned position in job applications, with total count", implementation = TopItem.class)
    private TopItem topPosition;

    @Schema(
            description = "Number of job applications per status",
            example = """
                        {
                          "APPLIED": 4,
                          "ACCEPTED": 2,
                          "REJECTED": 6
                        }
                    """
    )
    private Map<Status, Long> byStatus;
}
