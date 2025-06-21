package com.jobflow.user_service.user;

import com.jobflow.user_service.BaseIT;
import com.jobflow.user_service.TestUtil;
import com.jobflow.user_service.auth.AuthenticationRequest;
import com.jobflow.user_service.auth.AuthenticationResponse;
import com.jobflow.user_service.handler.ResponseError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

public class UserIT extends BaseIT {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Value("${notification.service.api-key}")
    private String apiKey;

    private User user;

    @BeforeEach
    public void setup() {
        userRepository.deleteAll();

        user = TestUtil.createUser();
    }

    @Test
    public void getUserInfo_returnUserInfo() {
        user.setId(null);
        User savedUser = userRepository.save(user);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-Key", apiKey);
        HttpEntity<Void> request = TestUtil.createRequest(headers);

        ResponseEntity<UserInfoDto> response = restTemplate.exchange(
                "/api/v1/users/info?userId=" + savedUser.getId(),
                HttpMethod.GET,
                request,
                UserInfoDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        UserInfoDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(user.getLogin(), responseBody.getEmail());
        assertNull(responseBody.getTelegramChatId());
    }

    @Test
    public void getUserInfo_invalidApiKey_returnUnauthorized() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-Key", "invalid-key");
        HttpEntity<Void> request = TestUtil.createRequest(headers);

        ResponseEntity<ResponseError> response = restTemplate.exchange(
                "/api/v1/users/info?userId=999",
                HttpMethod.GET,
                request,
                ResponseError.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

        ResponseError error = response.getBody();
        assertNotNull(error);
        assertEquals("Api key: invalid-key invalid", error.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), error.getStatus());
        assertNotNull(error.getTime());
    }

    @Test
    public void getUserInfo_userNotFound_returnNotFound() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-Key", apiKey);
        HttpEntity<Void> request = TestUtil.createRequest(headers);

        ResponseEntity<ResponseError> response = restTemplate.exchange(
                "/api/v1/users/info?userId=999",
                HttpMethod.GET,
                request,
                ResponseError.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        ResponseError error = response.getBody();
        assertNotNull(error);
        assertEquals("User with id: 999 not found", error.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), error.getStatus());
        assertNotNull(error.getTime());
    }
}
