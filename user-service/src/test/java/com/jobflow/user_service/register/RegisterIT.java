package com.jobflow.user_service.register;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobflow.user_service.BaseIT;
import com.jobflow.user_service.TestUtil;
import com.jobflow.user_service.email.EmailService;
import com.jobflow.user_service.handler.ResponseError;
import com.jobflow.user_service.jwt.JwtService;
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

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RegisterIT extends BaseIT {

    private static final String LOGIN = "ivanivanov@gmail.com";
    private static final String VERIFY_KEY = "email:verify:%s";
    private static final String DATA_KEY = "email:data:%s";
    private static final String CODE = "111111";

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

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private EmailService emailService;

    private RegisterRequest registerRequest;

    private ConfirmCodeRequest confirmCodeRequest;

    private ResendCodeRequest resendCodeRequest;

    private String registerRequestJson;

    @BeforeEach
    public void setup() throws JsonProcessingException {
        registerRequest = new RegisterRequest("Ivan", "Ivanov", LOGIN, "abcde");
        registerRequestJson = objectMapper.writeValueAsString(registerRequest);
        confirmCodeRequest = new ConfirmCodeRequest(LOGIN, Integer.parseInt(CODE));
        resendCodeRequest = new ResendCodeRequest(LOGIN);
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

        String redisCodeJson = redisTemplate.opsForValue().get(String.format(VERIFY_KEY, registerRequest.getLogin()));
        String redisDataJson = redisTemplate.opsForValue().get(String.format(DATA_KEY, registerRequest.getLogin()));
        assertNotNull(redisCodeJson);
        assertNotNull(redisDataJson);

        RegisterRequest savedRequest = objectMapper.readValue(redisDataJson, RegisterRequest.class);
        assertEquals(registerRequest.getFirstname(), savedRequest.getFirstname());
        assertEquals(registerRequest.getLastname(), savedRequest.getLastname());
        assertEquals(registerRequest.getLogin(), savedRequest.getLogin());
        assertTrue(passwordEncoder.matches("abcde", savedRequest.getPassword()));

        int code = Integer.parseInt(redisCodeJson);
        assertTrue(code >= 100_000 && code <= 999_999, "Verification code must be 6 digits");

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

    @Test
    public void confirmCode_successfullyConfirmCodeAndSaveUser() {
        saveDataInRedis(registerRequest.getLogin(), CODE, registerRequestJson);

        HttpEntity<ConfirmCodeRequest> request = TestUtil.createRequest(confirmCodeRequest);
        ResponseEntity<RegisterResponse> response = restTemplate.exchange(
                "/api/v1/register/confirm",
                HttpMethod.POST,
                request,
                RegisterResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        RegisterResponse responseBody = response.getBody();
        assertNotNull(responseBody);
        assertNotNull(responseBody.getAccessToken());
        assertNotNull(responseBody.getRefreshToken());

        String loginAccessToken = jwtService.extractLogin(responseBody.getAccessToken());
        String loginRefreshToken = jwtService.extractLogin(responseBody.getRefreshToken());
        assertEquals(LOGIN, loginAccessToken);
        assertEquals(LOGIN, loginRefreshToken);

        assertNull(redisTemplate.opsForValue().get(String.format(VERIFY_KEY, registerRequest.getLogin())));
        assertNull(redisTemplate.opsForValue().get(String.format(DATA_KEY, registerRequest.getLogin())));

        User savedUser = userRepository.findByLogin(LOGIN).get();
        assertNotNull(savedUser);
        assertEquals(registerRequest.getFirstname(), savedUser.getFirstname());
        assertEquals(registerRequest.getLastname(), savedUser.getLastname());
        assertEquals(registerRequest.getLogin(), savedUser.getLogin());
        assertEquals(registerRequest.getPassword(), savedUser.getPassword());
        assertEquals(Role.ROLE_USER, savedUser.getRole());
    }

    @Test
    public void confirmCode_invalidCode_returnBadRequest() {
        saveDataInRedis(registerRequest.getLogin(), "333333", registerRequestJson);

        HttpEntity<ConfirmCodeRequest> request = TestUtil.createRequest(confirmCodeRequest);
        ResponseEntity<ResponseError> response = restTemplate.exchange(
                "/api/v1/register/confirm",
                HttpMethod.POST,
                request,
                ResponseError.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        ResponseError error = response.getBody();
        assertEquals("Verification code: " + CODE + " invalid for user: " + LOGIN, error.getMessage());
        assertNotNull(error);
        assertNotNull(error.getTime());
        assertEquals(HttpStatus.BAD_REQUEST.value(), error.getStatus());

    }

    @Test
    public void confirmCode_invalidData_returnBadRequest() {
        ConfirmCodeRequest invalidRequest = new ConfirmCodeRequest("", 0);
        HttpEntity<ConfirmCodeRequest> request = TestUtil.createRequest(invalidRequest);
        ResponseEntity<ResponseError> response = restTemplate.exchange(
                "/api/v1/register/confirm",
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
    public void confirmCode_codeExpired_returnGone() {
        HttpEntity<ConfirmCodeRequest> request = TestUtil.createRequest(confirmCodeRequest);
        ResponseEntity<ResponseError> response = restTemplate.exchange(
                "/api/v1/register/confirm",
                HttpMethod.POST,
                request,
                ResponseError.class
        );

        assertEquals(HttpStatus.GONE, response.getStatusCode());

        ResponseError error = response.getBody();
        assertEquals("Verification code expired for user with login: " + LOGIN, error.getMessage());
        assertNotNull(error);
        assertNotNull(error.getTime());
        assertEquals(HttpStatus.GONE.value(), error.getStatus());
    }

    @Test
    public void resendCode_successfullyResendNewCode() {
        saveDataInRedis(registerRequest.getLogin(), "111111", registerRequestJson);

        HttpEntity<ResendCodeRequest> request = TestUtil.createRequest(resendCodeRequest);
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/register/resend",
                HttpMethod.POST,
                request,
                Void.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        String redisCodeJson = redisTemplate.opsForValue().get(String.format(VERIFY_KEY, registerRequest.getLogin()));
        String redisDataJson = redisTemplate.opsForValue().get(String.format(DATA_KEY, registerRequest.getLogin()));
        assertNotNull(redisCodeJson);
        assertNotNull(redisDataJson);

        int code = Integer.parseInt(redisCodeJson);
        assertTrue(code != 111111);
        assertTrue(code >= 100_000 && code <= 999_999, "Verification code must be 6 digits");

        verify(emailService, times(1)).sendCodeToEmail(eq(registerRequest.getLogin()), anyInt());
    }

    @Test
    public void resendCode_invalidData_returnBadRequest() {
        ResendCodeRequest invalidRequest = new ResendCodeRequest("");
        HttpEntity<ResendCodeRequest> request = TestUtil.createRequest(invalidRequest);
        ResponseEntity<ResponseError> response = restTemplate.exchange(
                "/api/v1/register/resend",
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
    public void resendCode_codeExpired_returnGone() {
        HttpEntity<ResendCodeRequest> request = TestUtil.createRequest(resendCodeRequest);
        ResponseEntity<ResponseError> response = restTemplate.exchange(
                "/api/v1/register/resend",
                HttpMethod.POST,
                request,
                ResponseError.class
        );

        assertEquals(HttpStatus.GONE, response.getStatusCode());

        ResponseError error = response.getBody();
        assertEquals("Verification code expired for user with login: " + LOGIN, error.getMessage());
        assertNotNull(error);
        assertNotNull(error.getTime());
        assertEquals(HttpStatus.GONE.value(), error.getStatus());
    }

    private void saveDataInRedis(String login, String code, String registerRequest) {
        redisTemplate.opsForValue().set(String.format(VERIFY_KEY, login), code, 5L, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(String.format(DATA_KEY, login), registerRequest, 5L, TimeUnit.SECONDS);
    }

    private void initDb() {
        User user = new User(null, "Ivan", "Ivanov", LOGIN, passwordEncoder.encode("abcde"), Role.ROLE_USER);
        userRepository.save(user);
    }

    private void cleanDb() {
        userRepository.deleteAll();
    }


}
