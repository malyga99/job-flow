package com.jobflow.notification_service.notification.history;

import com.jobflow.notification_service.BaseIT;
import com.jobflow.notification_service.JwtTestUtil;
import com.jobflow.notification_service.TestPageResponse;
import com.jobflow.notification_service.TestUtil;
import com.jobflow.notification_service.handler.ResponseError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class NotificationHistoryIT extends BaseIT {

    private static final Long USER_ID = TestUtil.USER_ID;

    @Autowired
    private NotificationHistoryRepository notificationHistoryRepository;

    @Autowired
    private JwtTestUtil jwtTestUtil;

    @Autowired
    private TestRestTemplate restTemplate;

    private NotificationHistory firstNotificationHistory;

    private NotificationHistory secondNotificationHistory;

    private NotificationHistory thirdNotificationHistory;

    private String token;

    @BeforeEach
    public void setup() {
        TestUtil.clearDb(notificationHistoryRepository);

        firstNotificationHistory = TestUtil.createNotificationHistory();
        secondNotificationHistory = TestUtil.createNotificationHistory();
        thirdNotificationHistory = TestUtil.createNotificationHistory();

        token = jwtTestUtil.generateToken(USER_ID);
    }

    @Test
    public void findMy_returnTwoJobApplicationsWithoutUnsuccessful() {
        thirdNotificationHistory.setSuccess(false);
        thirdNotificationHistory.setFailureReason("Some failure");
        firstNotificationHistory.setUserId(USER_ID);
        secondNotificationHistory.setUserId(USER_ID);
        thirdNotificationHistory.setUserId(USER_ID);


        TestUtil.saveDataInDb(notificationHistoryRepository, List.of(
                firstNotificationHistory, secondNotificationHistory, thirdNotificationHistory
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = TestUtil.createRequest(null, headers);

        ResponseEntity<TestPageResponse<NotificationHistoryDto>> response = restTemplate.exchange(
                "/api/v1/notifications/my",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<>() {
                }
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        TestPageResponse<NotificationHistoryDto> body = response.getBody();
        assertNotNull(body);
        assertEquals(2, body.getTotalElements());

        List<NotificationHistoryDto> content = response.getBody().getContent();
        assertNotNull(content);
        assertEquals(2, content.size());
        assertEquals(firstNotificationHistory.getNotificationType(), content.get(0).getNotificationType());
        assertEquals(firstNotificationHistory.getSubject(), content.get(0).getSubject());
        assertEquals(secondNotificationHistory.getNotificationType(), content.get(1).getNotificationType());
        assertEquals(secondNotificationHistory.getSubject(), content.get(1).getSubject());
    }

    @Test
    public void findMy_withNoNotifications_returnEmptyList() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = TestUtil.createRequest(null, headers);

        ResponseEntity<TestPageResponse<NotificationHistoryDto>> response = restTemplate.exchange(
                "/api/v1/notifications/my",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<>() {
                }
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        TestPageResponse<NotificationHistoryDto> body = response.getBody();
        assertNotNull(body);
        assertEquals(0, body.getTotalElements());

        List<NotificationHistoryDto> content = response.getBody().getContent();
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
}
