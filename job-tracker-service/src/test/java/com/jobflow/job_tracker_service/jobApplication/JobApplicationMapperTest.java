package com.jobflow.job_tracker_service.jobApplication;

import com.jobflow.job_tracker_service.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class JobApplicationMapperTest {

    @InjectMocks
    private JobApplicationMapper jobApplicationMapper;

    private JobApplicationCreateUpdateDto createUpdateDto;

    private JobApplication jobApplication;

    @BeforeEach
    public void setup() {
        createUpdateDto = TestUtil.createJobApplicationCreateUpdateDto();
        jobApplication = TestUtil.createJobApplication();
    }

    @Test
    public void toEntity_returnEntity() {
        JobApplication result = jobApplicationMapper.toEntity(createUpdateDto, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals(createUpdateDto.getCompany(), result.getCompany());
        assertEquals(createUpdateDto.getPosition(), result.getPosition());
        assertEquals(createUpdateDto.getLink(), result.getLink());
        assertEquals(createUpdateDto.getSource(), result.getSource());
        assertEquals(createUpdateDto.getSourceDetails(), result.getSourceDetails());
        assertEquals(createUpdateDto.getSalaryMin(), result.getSalaryMin());
        assertEquals(createUpdateDto.getSalaryMax(), result.getSalaryMax());
        assertEquals(createUpdateDto.getCurrency(), result.getCurrency());
        assertEquals(createUpdateDto.getStatus(), result.getStatus());
        assertEquals(createUpdateDto.getComment(), result.getComment());
        assertEquals(createUpdateDto.getAppliedAt(), result.getAppliedAt());
    }

    @Test
    public void toDto_returnDto() {
        JobApplicationDto result = jobApplicationMapper.toDto(jobApplication);

        assertNotNull(result);
        assertEquals(jobApplication.getId(), result.getId());
        assertEquals(jobApplication.getUserId(), result.getUserId());
        assertEquals(jobApplication.getCompany(), result.getCompany());
        assertEquals(jobApplication.getPosition(), result.getPosition());
        assertEquals(jobApplication.getLink(), result.getLink());
        assertEquals(jobApplication.getSource(), result.getSource());
        assertEquals(jobApplication.getSourceDetails(), result.getSourceDetails());
        assertEquals(jobApplication.getSalaryMin(), result.getSalaryMin());
        assertEquals(jobApplication.getSalaryMax(), result.getSalaryMax());
        assertEquals(jobApplication.getCurrency(), result.getCurrency());
        assertEquals(jobApplication.getStatus(), result.getStatus());
        assertEquals(jobApplication.getComment(), result.getComment());
        assertEquals(jobApplication.getAppliedAt(), result.getAppliedAt());
        assertEquals(jobApplication.getCreatedAt(), result.getCreatedAt());
        assertEquals(jobApplication.getUpdatedAt(), result.getUpdatedAt());
    }
}