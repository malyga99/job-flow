package com.jobflow.job_tracker_service.jobApplication;

public enum SortField {
    CREATED_AT("createdAt"),
    UPDATED_AT("updatedAt");

    private final String field;

    SortField(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
