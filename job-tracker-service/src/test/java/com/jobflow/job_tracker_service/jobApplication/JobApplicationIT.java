package com.jobflow.job_tracker_service.jobApplication;

import com.jobflow.job_tracker_service.BaseIT;
import com.jobflow.job_tracker_service.JwtTestUtil;
import com.jobflow.job_tracker_service.TestPageResponse;
import com.jobflow.job_tracker_service.TestUtil;
import com.jobflow.job_tracker_service.handler.ResponseError;
import com.jobflow.job_tracker_service.jobApplication.stats.StatsCacheKeyUtils;
import com.jobflow.job_tracker_service.notification.NotificationEvent;
import com.jobflow.job_tracker_service.notification.NotificationType;
import com.jobflow.job_tracker_service.rabbitMQ.RabbitProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class JobApplicationIT extends BaseIT {

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
    private AmqpTemplate amqpTemplate;

    @Autowired
    private RabbitProperties rabbitProperties;

    @Autowired
    private AmqpAdmin amqpAdmin;

    private String token;

    private JobApplicationCreateUpdateDto createUpdateDto;

    private JobApplication firstJobApplication;

    private JobApplication secondJobApplication;

    @BeforeEach
    public void setup() {
        TestUtil.clearDb(jobApplicationRepository);
        TestUtil.clearRabbit(amqpAdmin, rabbitProperties.getEmailQueueName());
        TestUtil.clearRabbit(amqpAdmin, rabbitProperties.getTelegramQueueName());
        TestUtil.clearKeys(redisTemplate, "rate_limiter:*");

        createUpdateDto = TestUtil.createJobApplicationCreateUpdateDto();
        firstJobApplication = TestUtil.createJobApplication();
        secondJobApplication = TestUtil.createJobApplication();
        firstJobApplication.setId(null);
        secondJobApplication.setId(null);

        token = jwtTestUtil.generateToken(USER_ID);
    }

    @Test
    public void findMy_returnTwoJobApplications() {
        firstJobApplication.setUserId(USER_ID);
        secondJobApplication.setUserId(USER_ID);
        TestUtil.saveDataInDb(jobApplicationRepository, List.of(firstJobApplication, secondJobApplication));

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

        firstJobApplication.setUserId(USER_ID);
        TestUtil.saveDataInDb(jobApplicationRepository, List.of(firstJobApplication));

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

        firstJobApplication.setUserId(999L);
        TestUtil.saveDataInDb(jobApplicationRepository, List.of(firstJobApplication));

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
    public void create_tooManyRequests_returnTooManyRequests() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<JobApplicationCreateUpdateDto> request = TestUtil.createRequest(createUpdateDto, headers);

        for (int i = 0; i < 5; i++) {
            ResponseEntity<JobApplicationDto> response = restTemplate.exchange(
                    "/api/v1/job-applications",
                    HttpMethod.POST,
                    request,
                    JobApplicationDto.class
            );

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
        }

        ResponseEntity<ResponseError> response = restTemplate.exchange(
                "/api/v1/job-applications",
                HttpMethod.POST,
                request,
                ResponseError.class
        );

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());

        ResponseError error = response.getBody();
        assertNotNull(error);
        assertEquals("Too many job applications created. Try again in a minute", error.getMessage());
        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), error.getStatus());
        assertNotNull(error.getTime());
    }

    @Test
    public void create_deleteStatsFromRedisCorrectly() {
        saveStatsInRedis(USER_ID);

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

        assertNull(redisTemplate.opsForValue().get(StatsCacheKeyUtils.keyForUser(USER_ID)));
    }

    @Test
    public void create_sendNotificationEventCorrectly() {
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

        NotificationEvent notificationEvent = amqpTemplate.receiveAndConvert(rabbitProperties.getEmailQueueName(),
                5000, new ParameterizedTypeReference<NotificationEvent>() {
                });
        assertNotNull(notificationEvent);
        assertEquals(notificationEvent.getUserId(), USER_ID);
        assertEquals(notificationEvent.getNotificationType(), NotificationType.EMAIL);
        assertEquals(notificationEvent.getSubject(), "You have responded to the job application");
        assertEquals(notificationEvent.getMessage(), String.format(
                "You have successfully submitted a response for the %s position to %s",
                createUpdateDto.getPosition(), createUpdateDto.getCompany()
        ));
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

    @Test
    public void update_updatesJobApplicationCorrectly() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<JobApplicationCreateUpdateDto> request = TestUtil.createRequest(createUpdateDto, headers);

        firstJobApplication.setUserId(USER_ID);
        TestUtil.saveDataInDb(jobApplicationRepository, List.of(firstJobApplication));

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/job-applications/" + firstJobApplication.getId(),
                HttpMethod.PUT,
                request,
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        JobApplication jobApplication = jobApplicationRepository.findById(firstJobApplication.getId()).get();
        assertEquals(createUpdateDto.getCompany(), jobApplication.getCompany());
        assertEquals(createUpdateDto.getPosition(), jobApplication.getPosition());
        assertEquals(createUpdateDto.getLink(), jobApplication.getLink());
        assertEquals(createUpdateDto.getSource(), jobApplication.getSource());
        assertEquals(createUpdateDto.getSalaryMin(), jobApplication.getSalaryMin());
        assertEquals(createUpdateDto.getSalaryMax(), jobApplication.getSalaryMax());
        assertEquals(createUpdateDto.getCurrency(), jobApplication.getCurrency());
        assertEquals(createUpdateDto.getStatus(), jobApplication.getStatus());
        assertEquals(createUpdateDto.getComment(), jobApplication.getComment());
        assertEquals(createUpdateDto.getAppliedAt(), jobApplication.getAppliedAt());
        assertNotNull(jobApplication.getId());
        assertNotNull(jobApplication.getUserId());
        assertNotNull(jobApplication.getCreatedAt());
        assertNotNull(jobApplication.getUpdatedAt());
    }

    @Test
    public void update_tooManyRequests_returnTooManyRequests() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<JobApplicationCreateUpdateDto> request = TestUtil.createRequest(createUpdateDto, headers);

        firstJobApplication.setUserId(USER_ID);
        TestUtil.saveDataInDb(jobApplicationRepository, List.of(firstJobApplication));

        for (int i = 0; i < 10; i++) {
            ResponseEntity<Void> response = restTemplate.exchange(
                    "/api/v1/job-applications/" + firstJobApplication.getId(),
                    HttpMethod.PUT,
                    request,
                    Void.class
            );

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        }

        ResponseEntity<ResponseError> response = restTemplate.exchange(
                "/api/v1/job-applications/" + firstJobApplication.getId(),
                HttpMethod.PUT,
                request,
                ResponseError.class
        );

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());

        ResponseError error = response.getBody();
        assertNotNull(error);
        assertEquals("Too many updates. Try again in a minute", error.getMessage());
        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), error.getStatus());
        assertNotNull(error.getTime());
    }

    @Test
    public void update_deleteStatsFromRedisCorrectly() {
        saveStatsInRedis(USER_ID);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<JobApplicationCreateUpdateDto> request = TestUtil.createRequest(createUpdateDto, headers);

        firstJobApplication.setUserId(USER_ID);
        TestUtil.saveDataInDb(jobApplicationRepository, List.of(firstJobApplication));

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/job-applications/" + firstJobApplication.getId(),
                HttpMethod.PUT,
                request,
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        assertNull(redisTemplate.opsForValue().get(StatsCacheKeyUtils.keyForUser(USER_ID)));
    }

    @Test
    public void update_sendNotificationEventCorrectly() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        createUpdateDto.setStatus(Status.APPLIED);
        HttpEntity<JobApplicationCreateUpdateDto> request = TestUtil.createRequest(createUpdateDto, headers);

        firstJobApplication.setUserId(USER_ID);
        TestUtil.saveDataInDb(jobApplicationRepository, List.of(firstJobApplication));

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/job-applications/" + firstJobApplication.getId(),
                HttpMethod.PUT,
                request,
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        NotificationEvent notificationEvent = amqpTemplate.receiveAndConvert(rabbitProperties.getTelegramQueueName(),
                5000, new ParameterizedTypeReference<NotificationEvent>() {
                });
        assertNotNull(notificationEvent);
        assertEquals(notificationEvent.getUserId(), USER_ID);
        assertEquals(notificationEvent.getNotificationType(), NotificationType.TELEGRAM);
        assertNull(notificationEvent.getSubject());
        assertEquals(notificationEvent.getMessage(), String.format(
                "The status of your %s job application has been updated: %s",
                createUpdateDto.getPosition(), createUpdateDto.getStatus()
        ));
    }

    @Test
    public void update_jobApplicationNotFound_returnNotFound() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<JobApplicationCreateUpdateDto> request = TestUtil.createRequest(createUpdateDto, headers);

        ResponseEntity<ResponseError> response = restTemplate.exchange(
                "/api/v1/job-applications/999",
                HttpMethod.PUT,
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
    public void update_notYourOwnJobApplication_returnForbidden() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<JobApplicationCreateUpdateDto> request = TestUtil.createRequest(createUpdateDto, headers);

        firstJobApplication.setUserId(999L);
        TestUtil.saveDataInDb(jobApplicationRepository, List.of(firstJobApplication));

        ResponseEntity<ResponseError> response = restTemplate.exchange(
                "/api/v1/job-applications/" + firstJobApplication.getId(),
                HttpMethod.PUT,
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
    public void update_withoutToken_returnUnauthorized() {
        ResponseEntity<ResponseError> response = restTemplate.exchange(
                "/api/v1/job-applications/1",
                HttpMethod.PUT,
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
    public void updateStatus_updatesStatusCorrectly() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = TestUtil.createRequest(null, headers);

        firstJobApplication.setUserId(USER_ID);
        TestUtil.saveDataInDb(jobApplicationRepository, List.of(firstJobApplication));

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/job-applications/" + firstJobApplication.getId() + "?status=" + Status.REJECTED,
                HttpMethod.PATCH,
                request,
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        JobApplication jobApplication = jobApplicationRepository.findById(firstJobApplication.getId()).get();
        assertEquals(Status.REJECTED, jobApplication.getStatus());
    }

    @Test
    public void updateStatus_tooManyRequests_returnTooManyRequests() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = TestUtil.createRequest(null, headers);

        firstJobApplication.setUserId(USER_ID);
        TestUtil.saveDataInDb(jobApplicationRepository, List.of(firstJobApplication));

        for (int i = 0; i < 10; i++) {
            ResponseEntity<Void> response = restTemplate.exchange(
                    "/api/v1/job-applications/" + firstJobApplication.getId() + "?status=" + Status.REJECTED,
                    HttpMethod.PATCH,
                    request,
                    Void.class
            );

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        }

        ResponseEntity<ResponseError> response = restTemplate.exchange(
                "/api/v1/job-applications/" + firstJobApplication.getId() + "?status=" + Status.REJECTED,
                HttpMethod.PATCH,
                request,
                ResponseError.class
        );

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());

        ResponseError error = response.getBody();
        assertNotNull(error);
        assertEquals("Too many status updates. Try again in a minute", error.getMessage());
        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), error.getStatus());
        assertNotNull(error.getTime());
    }

    @Test
    public void updateStatus_statusIsOffer_sendNotificationEventCorrectly() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = TestUtil.createRequest(null, headers);

        firstJobApplication.setUserId(USER_ID);
        TestUtil.saveDataInDb(jobApplicationRepository, List.of(firstJobApplication));

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/job-applications/" + firstJobApplication.getId() + "?status=" + Status.OFFER,
                HttpMethod.PATCH,
                request,
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        NotificationEvent notificationEventFromEmail = amqpTemplate.receiveAndConvert(rabbitProperties.getEmailQueueName(),
                5000, new ParameterizedTypeReference<NotificationEvent>() {
                });
        NotificationEvent notificationEventFromTelegram = amqpTemplate.receiveAndConvert(rabbitProperties.getTelegramQueueName(),
                5000, new ParameterizedTypeReference<NotificationEvent>() {
                });

        assertNotNull(notificationEventFromEmail);
        assertEquals(notificationEventFromEmail.getUserId(), USER_ID);
        assertEquals(notificationEventFromEmail.getNotificationType(), NotificationType.BOTH);
        assertEquals(notificationEventFromEmail.getSubject(), "Congratulations! You have received an offer");
        assertEquals(notificationEventFromEmail.getMessage(), String.format(
                "You have received an offer from %s for the position of %s",
                createUpdateDto.getCompany(), createUpdateDto.getPosition()
        ));
        assertNotNull(notificationEventFromTelegram);
        assertEquals(notificationEventFromTelegram.getUserId(), USER_ID);
        assertEquals(notificationEventFromTelegram.getNotificationType(), NotificationType.BOTH);
        assertEquals(notificationEventFromTelegram.getSubject(), "Congratulations! You have received an offer");
        assertEquals(notificationEventFromTelegram.getMessage(), String.format(
                "You have received an offer from %s for the position of %s",
                createUpdateDto.getCompany(), createUpdateDto.getPosition()
        ));
    }

    @Test
    public void updateStatus_statusIsRejected_sendNotificationEventCorrectly() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = TestUtil.createRequest(null, headers);

        firstJobApplication.setUserId(USER_ID);
        TestUtil.saveDataInDb(jobApplicationRepository, List.of(firstJobApplication));

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/job-applications/" + firstJobApplication.getId() + "?status=" + Status.REJECTED,
                HttpMethod.PATCH,
                request,
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        NotificationEvent notificationEvent = amqpTemplate.receiveAndConvert(rabbitProperties.getEmailQueueName(),
                5000, new ParameterizedTypeReference<NotificationEvent>() {
                });

        assertNotNull(notificationEvent);
        assertEquals(notificationEvent.getUserId(), USER_ID);
        assertEquals(notificationEvent.getNotificationType(), NotificationType.EMAIL);
        assertEquals(notificationEvent.getSubject(), "You job application has been rejected");
        assertEquals(notificationEvent.getMessage(), String.format(
                "Unfortunately, your job application for the %s position at %s has been rejected",
                createUpdateDto.getPosition(), createUpdateDto.getCompany()
        ));
    }

    @Test
    public void updateStatus_anotherStatus_sendNotificationEventCorrectly() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = TestUtil.createRequest(null, headers);

        firstJobApplication.setUserId(USER_ID);
        TestUtil.saveDataInDb(jobApplicationRepository, List.of(firstJobApplication));

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/job-applications/" + firstJobApplication.getId() + "?status=" + Status.APPLIED,
                HttpMethod.PATCH,
                request,
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        NotificationEvent notificationEvent = amqpTemplate.receiveAndConvert(rabbitProperties.getTelegramQueueName(),
                5000, new ParameterizedTypeReference<NotificationEvent>() {
                });

        assertNotNull(notificationEvent);
        assertEquals(notificationEvent.getUserId(), USER_ID);
        assertEquals(notificationEvent.getNotificationType(), NotificationType.TELEGRAM);
        assertNull(notificationEvent.getSubject());
        assertEquals(notificationEvent.getMessage(), String.format(
                "The status of your %s job application has been updated: %s",
                createUpdateDto.getPosition(), createUpdateDto.getStatus()
        ));
    }

    @Test
    public void updateStatus_deleteStatsFromRedisCorrectly() {
        saveStatsInRedis(USER_ID);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<JobApplicationCreateUpdateDto> request = TestUtil.createRequest(createUpdateDto, headers);

        firstJobApplication.setUserId(USER_ID);
        TestUtil.saveDataInDb(jobApplicationRepository, List.of(firstJobApplication));

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/job-applications/" + firstJobApplication.getId() + "?status=" + Status.REJECTED,
                HttpMethod.PATCH,
                request,
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        assertNull(redisTemplate.opsForValue().get(StatsCacheKeyUtils.keyForUser(USER_ID)));
    }

    @Test
    public void updateStatus_jobApplicationNotFound_returnNotFound() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<JobApplicationCreateUpdateDto> request = TestUtil.createRequest(createUpdateDto, headers);

        ResponseEntity<ResponseError> response = restTemplate.exchange(
                "/api/v1/job-applications/999" + "?status=" + Status.REJECTED,
                HttpMethod.PATCH,
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
    public void updateStatus_notYourOwnJobApplication_returnForbidden() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<JobApplicationCreateUpdateDto> request = TestUtil.createRequest(createUpdateDto, headers);

        firstJobApplication.setUserId(999L);
        TestUtil.saveDataInDb(jobApplicationRepository, List.of(firstJobApplication));

        ResponseEntity<ResponseError> response = restTemplate.exchange(
                "/api/v1/job-applications/" + firstJobApplication.getId() + "?status=" + Status.REJECTED,
                HttpMethod.PATCH,
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
    public void updateStatus_withoutToken_returnUnauthorized() {
        ResponseEntity<ResponseError> response = restTemplate.exchange(
                "/api/v1/job-applications/1" + "?status=" + Status.REJECTED,
                HttpMethod.PATCH,
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
    public void delete_deletesJobApplicationCorrectly() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = TestUtil.createRequest(null, headers);

        firstJobApplication.setUserId(USER_ID);
        TestUtil.saveDataInDb(jobApplicationRepository, List.of(firstJobApplication));
        assertTrue(jobApplicationRepository.findById(firstJobApplication.getId()).isPresent());

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/job-applications/" + firstJobApplication.getId(),
                HttpMethod.DELETE,
                request,
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        assertFalse(jobApplicationRepository.findById(firstJobApplication.getId()).isPresent());
        assertEquals(0, jobApplicationRepository.findAll().size());
    }

    @Test
    public void delete_tooManyRequests_returnTooManyRequests() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = TestUtil.createRequest(null, headers);

        firstJobApplication.setUserId(USER_ID);

        for (int i = 0; i < 3; i++) {
            firstJobApplication.setId(null);
            TestUtil.saveDataInDb(jobApplicationRepository, List.of(firstJobApplication));

            ResponseEntity<Void> response = restTemplate.exchange(
                    "/api/v1/job-applications/" + firstJobApplication.getId(),
                    HttpMethod.DELETE,
                    request,
                    Void.class
            );

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        }

        ResponseEntity<ResponseError> response = restTemplate.exchange(
                "/api/v1/job-applications/" + firstJobApplication.getId(),
                HttpMethod.DELETE,
                request,
                ResponseError.class
        );

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());

        ResponseError error = response.getBody();
        assertNotNull(error);
        assertEquals("Too many delete attempts. Try again in a minute", error.getMessage());
        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), error.getStatus());
        assertNotNull(error.getTime());
    }

    @Test
    public void delete_deleteStatsFromRedisCorrectly() {
        saveStatsInRedis(USER_ID);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<JobApplicationCreateUpdateDto> request = TestUtil.createRequest(createUpdateDto, headers);

        firstJobApplication.setUserId(USER_ID);
        TestUtil.saveDataInDb(jobApplicationRepository, List.of(firstJobApplication));

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/job-applications/" + firstJobApplication.getId(),
                HttpMethod.DELETE,
                request,
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        assertNull(redisTemplate.opsForValue().get(StatsCacheKeyUtils.keyForUser(USER_ID)));
    }

    @Test
    public void delete_jobApplicationNotFound_returnNotFound() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = TestUtil.createRequest(null, headers);

        ResponseEntity<ResponseError> response = restTemplate.exchange(
                "/api/v1/job-applications/999",
                HttpMethod.DELETE,
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
    public void delete_notYourOwnJobApplication_returnForbidden() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = TestUtil.createRequest(null, headers);

        firstJobApplication.setUserId(999L);
        TestUtil.saveDataInDb(jobApplicationRepository, List.of(firstJobApplication));

        ResponseEntity<ResponseError> response = restTemplate.exchange(
                "/api/v1/job-applications/" + firstJobApplication.getId(),
                HttpMethod.DELETE,
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
    public void delete_withoutToken_returnUnauthorized() {
        ResponseEntity<ResponseError> response = restTemplate.exchange(
                "/api/v1/job-applications/1",
                HttpMethod.DELETE,
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

    private void saveStatsInRedis(Long userId) {
        redisTemplate.opsForValue().set(StatsCacheKeyUtils.keyForUser(userId), "someStats", Duration.ofHours(1L));

        assertNotNull(redisTemplate.opsForValue().get(StatsCacheKeyUtils.keyForUser(USER_ID)));
    }
}
