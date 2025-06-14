package com.jobflow.job_tracker_service.jobApplication.stats;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Top item with name and total count (e.g., top company or position)")
public class TopItem {

    @Schema(description = "Name of the top item", example = "Google")
    private String name;

    @Schema(description = "Total number of times this item appears", example = "1")
    private Long total;
}
