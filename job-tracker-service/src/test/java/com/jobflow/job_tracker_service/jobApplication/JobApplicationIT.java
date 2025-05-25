package com.jobflow.job_tracker_service.jobApplication;

import com.jobflow.job_tracker_service.BaseIT;
import com.jobflow.job_tracker_service.JwtTestUtil;
import com.jobflow.job_tracker_service.TestUtil;
import com.jobflow.job_tracker_service.handler.ResponseError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class JobApplicationIT extends BaseIT {

    @Autowired
    private JobApplicationRepository jobApplicationRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtTestUtil jwtTestUtil;

    private String token;

    private JobApplicationCreateUpdateDto createUpdateDto;

    @BeforeEach
    public void setup() {
        clearDb();

        createUpdateDto = TestUtil.createJobApplicationCreateUpdateDto();

        token = jwtTestUtil.generateToken(1L);
    }

    @Test
    public void create_returnCreatedJobApplication() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<JobApplicationCreateUpdateDto> request = TestUtil.createRequest(createUpdateDto, headers);

        ResponseEntity<JobApplicationDto> response = restTemplate.exchange(
                "/api/v1/job-applications",
                HttpMethod.POST,
                request,
                JobApplicationDto.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getHeaders().containsKey("Location"));

        JobApplicationDto savedJobApplication = response.getBody();
        assertNotNull(savedJobApplication);

        assertNotNull(savedJobApplication.getId());
        assertNotNull(savedJobApplication.getUserId());
        assertNotNull(savedJobApplication.getCreatedAt());
        assertNotNull(savedJobApplication.getUpdatedAt());
        assertEquals(createUpdateDto.getCompany(), savedJobApplication.getCompany());
        assertEquals(createUpdateDto.getPosition(), savedJobApplication.getPosition());
        assertEquals(createUpdateDto.getLink(), savedJobApplication.getLink());
        assertEquals(createUpdateDto.getSource(), savedJobApplication.getSource());
        assertEquals(createUpdateDto.getSourceDetails(), savedJobApplication.getSourceDetails());
        assertEquals(createUpdateDto.getSalaryMin(), savedJobApplication.getSalaryMin());
        assertEquals(createUpdateDto.getSalaryMax(), savedJobApplication.getSalaryMax());
        assertEquals(createUpdateDto.getCurrency(), savedJobApplication.getCurrency());
        assertEquals(createUpdateDto.getStatus(), savedJobApplication.getStatus());
        assertEquals(createUpdateDto.getComment(), savedJobApplication.getComment());
        assertEquals(createUpdateDto.getAppliedAt(), savedJobApplication.getAppliedAt());

        Optional<JobApplication> jobApplicationFromDb = jobApplicationRepository.findById(savedJobApplication.getId());
        assertTrue(jobApplicationFromDb.isPresent());
    }

    @Test
    public void create_withoutToken_returnUnauthorized() {
        HttpEntity<JobApplicationCreateUpdateDto> request = TestUtil.createRequest(createUpdateDto);

        ResponseEntity<ResponseError> response = restTemplate.exchange(
                "/api/v1/job-applications",
                HttpMethod.POST,
                request,
                ResponseError.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

        ResponseError error = response.getBody();
        assertNotNull(error);
        assertNotNull(error.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), error.getStatus());
        assertNotNull(error.getTime());
    }

    private void clearDb() {
        jobApplicationRepository.deleteAll();
    }
}
