package com.jobflow.job_tracker_service.jobApplication;

import com.jobflow.job_tracker_service.BaseIT;
import com.jobflow.job_tracker_service.JwtTestUtil;
import com.jobflow.job_tracker_service.TestPageResponse;
import com.jobflow.job_tracker_service.TestUtil;
import com.jobflow.job_tracker_service.handler.ResponseError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class JobApplicationIT extends BaseIT {

    private static final Long USER_ID = 1L;

    @Autowired
    private JobApplicationRepository jobApplicationRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtTestUtil jwtTestUtil;

    private String token;

    private JobApplicationCreateUpdateDto createUpdateDto;

    private JobApplication firstJobApplication;

    private JobApplication secondJobApplication;

    @BeforeEach
    public void setup() {
        clearDb();

        createUpdateDto = TestUtil.createJobApplicationCreateUpdateDto();
        firstJobApplication = TestUtil.createJobApplication();
        secondJobApplication = TestUtil.createJobApplication();

        token = jwtTestUtil.generateToken(USER_ID);


    }

    @Test
    public void findMy_returnTwoJobApplications() {
        firstJobApplication.setId(null);
        secondJobApplication.setId(null);
        firstJobApplication.setUserId(USER_ID);
        secondJobApplication.setUserId(USER_ID);
        saveJobApplications(firstJobApplication, secondJobApplication);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = TestUtil.createRequest(null, headers);

        ResponseEntity<TestPageResponse<JobApplicationDto>> response = restTemplate.exchange(
                "/api/v1/job-applications/my",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<>() {
                }
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        TestPageResponse<JobApplicationDto> body = response.getBody();
        assertNotNull(body);
        assertEquals(2, body.getTotalElements());

        List<JobApplicationDto> content = response.getBody().getContent();
        assertNotNull(content);
        assertEquals(2, content.size());
        assertEquals(firstJobApplication.getUserId(), content.get(0).getUserId());
        assertEquals(firstJobApplication.getCompany(), content.get(0).getCompany());
        assertEquals(secondJobApplication.getUserId(), content.get(1).getUserId());
        assertEquals(secondJobApplication.getCompany(), content.get(1).getCompany());
    }

    @Test
    public void findMy_withNoJobApplications_returnEmptyList() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = TestUtil.createRequest(null, headers);

        ResponseEntity<TestPageResponse<JobApplicationDto>> response = restTemplate.exchange(
                "/api/v1/job-applications/my",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<>() {
                }
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        TestPageResponse<JobApplicationDto> body = response.getBody();
        assertNotNull(body);
        assertEquals(0, body.getTotalElements());

        List<JobApplicationDto> content = response.getBody().getContent();
        assertNotNull(content);
        assertEquals(0, content.size());
    }

    @Test
    public void findMy_withoutToken_returnUnauthorized() {
        ResponseEntity<ResponseError> response = restTemplate.exchange(
                "/api/v1/job-applications/my",
                HttpMethod.GET,
                null,
                ResponseError.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

        ResponseError error = response.getBody();
        assertNotNull(error);
        assertNotNull(error.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), error.getStatus());
        assertNotNull(error.getTime());
    }

    @Test
    public void findById_returnJobApplication() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = TestUtil.createRequest(null, headers);

        firstJobApplication.setId(null);
        firstJobApplication.setUserId(USER_ID);
        saveJobApplications(firstJobApplication);

        ResponseEntity<JobApplicationDto> response = restTemplate.exchange(
                "/api/v1/job-applications/" + firstJobApplication.getId(),
                HttpMethod.GET,
                request,
                JobApplicationDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        JobApplicationDto result = response.getBody();
        assertNotNull(result);

        assertEquals(firstJobApplication.getId(), result.getId());
        assertEquals(firstJobApplication.getUserId(), result.getUserId());
        assertEquals(firstJobApplication.getCompany(), result.getCompany());
        assertEquals(firstJobApplication.getPosition(), result.getPosition());
        assertEquals(firstJobApplication.getLink(), result.getLink());
        assertEquals(firstJobApplication.getSource(), result.getSource());
        assertEquals(firstJobApplication.getSourceDetails(), result.getSourceDetails());
        assertEquals(firstJobApplication.getSalaryMin(), result.getSalaryMin());
        assertEquals(firstJobApplication.getSalaryMax(), result.getSalaryMax());
        assertEquals(firstJobApplication.getCurrency(), result.getCurrency());
        assertEquals(firstJobApplication.getStatus(), result.getStatus());
        assertEquals(firstJobApplication.getComment(), result.getComment());
        assertEquals(firstJobApplication.getAppliedAt(), result.getAppliedAt());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
    }

    @Test
    public void findById_jobApplicationNotFound_returnNotFound() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = TestUtil.createRequest(null, headers);

        ResponseEntity<ResponseError> response = restTemplate.exchange(
                "/api/v1/job-applications/999",
                HttpMethod.GET,
                request,
                ResponseError.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        ResponseError error = response.getBody();
        assertNotNull(error);
        assertEquals("Job application with id: 999 not found", error.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), error.getStatus());
        assertNotNull(error.getTime());
    }

    @Test
    public void findById_notYourOwnJobApplication_returnForbidden() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = TestUtil.createRequest(null, headers);

        firstJobApplication.setId(null);
        firstJobApplication.setUserId(999L);
        saveJobApplications(firstJobApplication);

        ResponseEntity<ResponseError> response = restTemplate.exchange(
                "/api/v1/job-applications/" + firstJobApplication.getId(),
                HttpMethod.GET,
                request,
                ResponseError.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        ResponseError error = response.getBody();
        assertNotNull(error);
        assertNotNull(error.getMessage());
        assertEquals(HttpStatus.FORBIDDEN.value(), error.getStatus());
        assertNotNull(error.getTime());
    }

    @Test
    public void findById_withoutToken_returnUnauthorized() {
        ResponseEntity<ResponseError> response = restTemplate.exchange(
                "/api/v1/job-applications/1",
                HttpMethod.GET,
                null,
                ResponseError.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

        ResponseError error = response.getBody();
        assertNotNull(error);
        assertNotNull(error.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), error.getStatus());
        assertNotNull(error.getTime());
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

    private void saveJobApplications(JobApplication... jobApplications) {
        jobApplicationRepository.saveAll(Arrays.asList(jobApplications));
    }

    private void clearDb() {
        jobApplicationRepository.deleteAll();
    }
}
