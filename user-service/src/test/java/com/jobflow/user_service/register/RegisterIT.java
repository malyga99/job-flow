package com.jobflow.user_service.register;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobflow.user_service.BaseIT;
import com.jobflow.user_service.TestUtil;
import com.jobflow.user_service.auth.AuthenticationRequest;
import com.jobflow.user_service.email.EmailService;
import com.jobflow.user_service.handler.ResponseError;
import com.jobflow.user_service.user.Role;
import com.jobflow.user_service.user.User;
import com.jobflow.user_service.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RegisterIT extends BaseIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private EmailService emailService;

    private RegisterRequest registerRequest;

    private static final String LOGIN = "ivanivanov@gmail.com";

    @BeforeEach
    public void setup() {
        registerRequest = new RegisterRequest("Ivan", "Ivanov", LOGIN, "abcde");
        cleanDb();
    }

    @Test
    public void register_successfullySentCodeAndSaveInRedis() throws JsonProcessingException {
        HttpEntity<RegisterRequest> request = TestUtil.createRequest(registerRequest);
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/register",
                HttpMethod.POST,
                request,
                Void.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        String redisCodeJson = redisTemplate.opsForValue().get("email:verify:" + registerRequest.getLogin());
        String redisDataJson = redisTemplate.opsForValue().get("email:data:" + registerRequest.getLogin());
        assertNotNull(redisCodeJson);
        assertNotNull(redisDataJson);

        RegisterRequest savedRequest = objectMapper.readValue(redisDataJson, RegisterRequest.class);
        assertEquals(registerRequest.getFirstname(), savedRequest.getFirstname());
        assertEquals(registerRequest.getLastname(), savedRequest.getLastname());
        assertEquals(registerRequest.getLogin(), savedRequest.getLogin());
        assertTrue(passwordEncoder.matches("abcde", savedRequest.getPassword()));

        int code = Integer.parseInt(redisCodeJson);
        assertTrue(code >= 100000 && code <= 999999); //6 digits

        verify(emailService, times(1)).sendCodeToEmail(eq(registerRequest.getLogin()), anyInt());
    }

    @Test
    public void register_invalidData_returnBadRequest() {
        RegisterRequest invalidRequest = new RegisterRequest("", "", "", "");
        HttpEntity<RegisterRequest> request = TestUtil.createRequest(invalidRequest);
        ResponseEntity<ResponseError> response = restTemplate.exchange(
                "/api/v1/register",
                HttpMethod.POST,
                request,
                ResponseError.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        ResponseError error = response.getBody();
        assertNotNull(error);
        assertNotNull(error.getMessage());
        assertNotNull(error.getTime());
        assertEquals(HttpStatus.BAD_REQUEST.value(), error.getStatus());
    }

    @Test
    public void register_userAlreadyExists_returnBadRequest() {
        initDb();

        HttpEntity<RegisterRequest> request = TestUtil.createRequest(registerRequest);
        ResponseEntity<ResponseError> response = restTemplate.exchange(
                "/api/v1/register",
                HttpMethod.POST,
                request,
                ResponseError.class
        );

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());

        ResponseError error = response.getBody();
        assertNotNull(error);
        assertEquals("User with login: " + registerRequest.getLogin() + " already exists", error.getMessage());
        assertNotNull(error.getTime());
        assertEquals(HttpStatus.CONFLICT.value(), error.getStatus());

        String redisCodeJson = redisTemplate.opsForValue().get("email:verify:" + registerRequest.getLogin());
        String redisDataJson = redisTemplate.opsForValue().get("email:data:" + registerRequest.getLogin());
        assertNull(redisCodeJson);
        assertNull(redisDataJson);
        verifyNoInteractions(emailService);
    }

    private void initDb() {
        User user = new User(null, "Ivan", "Ivanov", LOGIN, passwordEncoder.encode("abcde"), Role.ROLE_USER);
        userRepository.save(user);
    }

    private void cleanDb() {
        userRepository.deleteAll();
    }


}
