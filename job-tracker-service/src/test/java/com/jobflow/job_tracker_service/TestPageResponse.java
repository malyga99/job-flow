package com.jobflow.job_tracker_service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * A wrapper for a paginated response.
 * It is used as an alternative to the Spring Data Page<T> to avoid
 * serialization/deserialization issues and provide a clean response structure.
 *
 * @param <T> Page content type
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestPageResponse<T>{
    private List<T> content;
    private int totalPages;
    private long totalElements;
    private boolean first;
    private boolean last;
    private int size;
    private int number;
}

