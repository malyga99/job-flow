package com.jobflow.notification_service.user;

import com.jobflow.notification_service.TestUtil;
import com.jobflow.notification_service.exception.UserClientException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RestUserClientTest {

    @Mock
    private UserServiceProperties userServiceProperties;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private RestUserClient restUserClient;

    private UserInfo userInfo;

    @BeforeEach
    public void setup() {
        userInfo = TestUtil.createUserInfo();
    }

    @Test
    public void getUserInfo_returnUserInfo() {
        ResponseEntity<UserInfo> response = new ResponseEntity<>(userInfo, HttpStatus.OK);
        ArgumentCaptor<HttpEntity<Void>> captor = ArgumentCaptor.forClass(HttpEntity.class);

        when(userServiceProperties.getApiKey()).thenReturn("test-key");
        when(userServiceProperties.getHost()).thenReturn("localhost");
        when(userServiceProperties.getPort()).thenReturn("8080");
        when(restTemplate.exchange(
                eq("http://localhost:8080/api/v1/users/info?userId=1"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(UserInfo.class)
        )).thenReturn(response);

        UserInfo result = restUserClient.getUserInfo(1L);
        assertNotNull(result);
        assertEquals(userInfo, result);

        verify(restTemplate, times(1)).exchange(
                eq("http://localhost:8080/api/v1/users/info?userId=1"),
                eq(HttpMethod.GET),
                captor.capture(),
                eq(UserInfo.class)
        );

        HttpEntity<Void> request = captor.getValue();
        HttpHeaders headers = request.getHeaders();
        assertEquals("test-key", headers.getFirst("X-API-Key"));
    }

    @Test
    public void getUserInfo_ifFailedToFetch_throwExc() {
        var httpStatusCodeException = new HttpStatusCodeException(HttpStatus.UNAUTHORIZED){};

        when(userServiceProperties.getApiKey()).thenReturn("test-key");
        when(userServiceProperties.getHost()).thenReturn("localhost");
        when(userServiceProperties.getPort()).thenReturn("8080");
        when(restTemplate.exchange(
                eq("http://localhost:8080/api/v1/users/info?userId=1"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(UserInfo.class)
        )).thenThrow(httpStatusCodeException);

        var userClientException = assertThrows(UserClientException.class, () -> restUserClient.getUserInfo(1L));
        assertEquals("Failed to fetch user info, status: " + httpStatusCodeException.getStatusCode(),
                userClientException.getMessage());
    }

}