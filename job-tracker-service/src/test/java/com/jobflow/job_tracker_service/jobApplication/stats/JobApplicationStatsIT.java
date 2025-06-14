package com.jobflow.job_tracker_service.jobApplication.stats;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobflow.job_tracker_service.BaseIT;
import com.jobflow.job_tracker_service.JwtTestUtil;
import com.jobflow.job_tracker_service.TestUtil;
import com.jobflow.job_tracker_service.handler.ResponseError;
import com.jobflow.job_tracker_service.jobApplication.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JobApplicationStatsIT extends BaseIT {

    private static final Long USER_ID = TestUtil.USER_ID;

    @Autowired
    private JobApplicationRepository jobApplicationRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtTestUtil jwtTestUtil;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;

    private JobApplication firstJobApplication;

    private JobApplication secondJobApplication;

    private JobApplication thirdJobApplication;

    @BeforeEach
    public void setup() {
        TestUtil.clearDb(jobApplicationRepository);
        TestUtil.clearKeys(redisTemplate, "jobAppStats:*");
        TestUtil.clearKeys(redisTemplate, "rate_limiter:*");

        firstJobApplication = TestUtil.createJobApplication();
        secondJobApplication = TestUtil.createJobApplication();
        thirdJobApplication = TestUtil.createJobApplication();
        firstJobApplication.setId(null);
        secondJobApplication.setId(null);
        thirdJobApplication.setId(null);

        token = jwtTestUtil.generateToken(USER_ID);
    }

    @Test
    public void getStats_returnCorrectlyStatsAndCacheInRedis() throws JsonProcessingException {
        firstJobApplication.setUserId(USER_ID);
        firstJobApplication.setCompany("Google");
        firstJobApplication.setPosition("Software Engineer");
        firstJobApplication.setStatus(Status.VIEWED);
        firstJobApplication.setAppliedAt(LocalDate.now().minusDays(6));

        secondJobApplication.setUserId(USER_ID);
        secondJobApplication.setCompany("google ");
        secondJobApplication.setPosition("Software Engineer");
        secondJobApplication.setStatus(Status.REJECTED);
        secondJobApplication.setAppliedAt(LocalDate.now().minusDays(15));

        thirdJobApplication.setUserId(USER_ID);
        thirdJobApplication.setCompany("Amazon");
        thirdJobApplication.setPosition("Backend Developer");
        thirdJobApplication.setStatus(Status.ACCEPTED);
        thirdJobApplication.setAppliedAt(LocalDate.now().minusDays(40));

        TestUtil.saveDataInDb(jobApplicationRepository, List.of(firstJobApplication, secondJobApplication, thirdJobApplication));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = TestUtil.createRequest(null, headers);

        ResponseEntity<JobApplicationStatsDto> response = restTemplate.exchange(
                "/api/v1/job-applications/stats",
                HttpMethod.GET,
                request,
                JobApplicationStatsDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        JobApplicationStatsDto body = response.getBody();
        assertNotNull(body);
        assertEquals(3L, body.getTotal());
        assertEquals(1L, body.getLast7Days());
        assertEquals(2L, body.getLast30Days());
        assertEquals(2L, body.getUniqueCompanies());
        assertEquals("Google", body.getTopCompany().getName());
        assertEquals(2L, body.getTopCompany().getTotal());
        assertEquals("Software Engineer", body.getTopPosition().getName());
        assertEquals(2L, body.getTopPosition().getTotal());
        assertEquals(1L, body.getByStatus().get(Status.VIEWED));
        assertEquals(1L, body.getByStatus().get(Status.REJECTED));
        assertEquals(1L, body.getByStatus().get(Status.ACCEPTED));

        String cachedStats = redisTemplate.opsForValue().get(StatsCacheKeyUtils.keyForUser(USER_ID));
        assertNotNull(cachedStats);

        JobApplicationStatsDto statsDto = objectMapper.readValue(cachedStats, JobApplicationStatsDto.class);
        assertEquals(body, statsDto);
    }

    @Test
    public void getStats_tooManyRequests_returnTooManyRequests() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = TestUtil.createRequest(null, headers);

        for (int i = 0; i < 5; i++) {
            ResponseEntity<JobApplicationStatsDto> response = restTemplate.exchange(
                    "/api/v1/job-applications/stats",
                    HttpMethod.GET,
                    request,
                    JobApplicationStatsDto.class
            );

            assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        ResponseEntity<ResponseError> response = restTemplate.exchange(
                "/api/v1/job-applications/stats",
                HttpMethod.GET,
                request,
                ResponseError.class
        );

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());

        ResponseError error = response.getBody();
        assertNotNull(error);
        assertEquals("Too many stats requests. Try again in a minute", error.getMessage());
        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), error.getStatus());
        assertNotNull(error.getTime());
    }
}
