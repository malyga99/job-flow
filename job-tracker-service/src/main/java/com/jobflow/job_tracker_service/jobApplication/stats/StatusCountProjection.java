package com.jobflow.job_tracker_service.jobApplication.stats;

import com.jobflow.job_tracker_service.jobApplication.Status;

public interface StatusCountProjection {
    Status getStatus();

    Long getTotal();
}
