package com.jobflow.user_service.email;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobflow.user_service.exception.EmailServiceException;
import com.jobflow.user_service.exception.InvalidVerificationCodeException;
import com.jobflow.user_service.exception.VerificationCodeExpiredException;
import com.jobflow.user_service.register.ConfirmCodeRequest;
import com.jobflow.user_service.register.RegisterRequest;
import com.jobflow.user_service.register.ResendCodeRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceImplTest {

    private static final String LOGIN = "IvanIvanov@gmail.com";
    private static final  String VERIFY_KEY = "email:verify:" + LOGIN;
    private static final String DATA_KEY = "email:data:" + LOGIN;

    @Mock
    private EmailService emailService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private EmailVerificationServiceImpl emailVerificationService;

    private RegisterRequest registerRequest;

    private ConfirmCodeRequest confirmCodeRequest;

    private ResendCodeRequest resendCodeRequest;

    private String registerRequestJson;

    @BeforeEach
    public void setup() {
        registerRequest = new RegisterRequest("Ivan", "Ivanov", LOGIN, "abcde");
        registerRequestJson = "{\"register\":\"json\"}";
        confirmCodeRequest = new ConfirmCodeRequest(LOGIN, 111111);
        resendCodeRequest = new ResendCodeRequest(LOGIN);
    }

    @Test
    public void sendVerificationCode_sendCodeSuccessfullyAndSaveInRedis() throws JsonProcessingException {
        doNothing().when(emailService).sendCodeToEmail(eq(LOGIN), anyInt());
        when(objectMapper.writeValueAsString(registerRequest)).thenReturn(registerRequestJson);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        emailVerificationService.sendVerificationCode(registerRequest);

        verify(emailService, times(1)).sendCodeToEmail(eq(LOGIN), anyInt());
        verify(valueOperations, times(1)).set(
                eq(VERIFY_KEY),
                anyString(),
                eq(5L),
                eq(TimeUnit.MINUTES)
        );
        verify(valueOperations, times(1)).set(
                eq(DATA_KEY),
                eq(registerRequestJson),
                eq(5L),
                eq(TimeUnit.MINUTES)
        );
    }

    @Test
    public void sendVerificationCode_jsonProcessingException_throwExc() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(registerRequest)).thenThrow(new JsonProcessingException("") {});

        var emailServiceException = assertThrows(EmailServiceException.class, () -> emailVerificationService.sendVerificationCode(registerRequest));
        assertEquals("Failed to serialize register request", emailServiceException.getMessage());

        verifyNoInteractions(redisTemplate, valueOperations, emailService);
    }

    @Test
    public void validateVerificationCode_returnRegisterRequest() throws JsonProcessingException {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(VERIFY_KEY)).thenReturn("111111");
        when(valueOperations.get(DATA_KEY)).thenReturn(registerRequestJson);
        when(objectMapper.readValue(registerRequestJson, RegisterRequest.class)).thenReturn(registerRequest);

        RegisterRequest result = emailVerificationService.validateVerificationCode(confirmCodeRequest);

        assertNotNull(result);
        assertEquals(registerRequest, result);

        verify(redisTemplate, times(1)).delete(VERIFY_KEY);
        verify(redisTemplate, times(1)).delete(DATA_KEY);
        verify(objectMapper, times(1)).readValue(registerRequestJson, RegisterRequest.class);
    }

    @Test
    public void validateVerificationCode_codeNotFound_throwExc() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(VERIFY_KEY)).thenReturn(null);
        when(valueOperations.get(DATA_KEY)).thenReturn(registerRequestJson);

        var verificationCodeExpiredException = assertThrows(VerificationCodeExpiredException.class, () -> emailVerificationService.validateVerificationCode(confirmCodeRequest));
        assertEquals("Verification code expired for user with login: " + LOGIN, verificationCodeExpiredException.getMessage());

        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    public void validateVerificationCode_dataNotFound_throwExc() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(VERIFY_KEY)).thenReturn("111111");
        when(valueOperations.get(DATA_KEY)).thenReturn(null);

        var verificationCodeExpiredException = assertThrows(VerificationCodeExpiredException.class, () -> emailVerificationService.validateVerificationCode(confirmCodeRequest));
        assertEquals("Verification code expired for user with login: " + LOGIN, verificationCodeExpiredException.getMessage());

        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    public void validateVerificationCode_invalidCode_throwExc() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(VERIFY_KEY)).thenReturn("333333");
        when(valueOperations.get(DATA_KEY)).thenReturn(registerRequestJson);

        var verificationCodeExpiredException = assertThrows(InvalidVerificationCodeException.class, () -> emailVerificationService.validateVerificationCode(confirmCodeRequest));
        assertEquals("Verification code: " + confirmCodeRequest.getCode() + " invalid for user: " + LOGIN, verificationCodeExpiredException.getMessage());

        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    public void validateVerificationCode_jsonProcessingException_throwExc() throws JsonProcessingException {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(VERIFY_KEY)).thenReturn("111111");
        when(valueOperations.get(DATA_KEY)).thenReturn(registerRequestJson);
        when(objectMapper.readValue(registerRequestJson, RegisterRequest.class)).thenThrow(new JsonProcessingException("") {});

        var emailServiceException = assertThrows(EmailServiceException.class, () -> emailVerificationService.validateVerificationCode(confirmCodeRequest));
        assertEquals("Failed to deserialize register request", emailServiceException.getMessage());

        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    public void resendCode_resendNewCode() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(VERIFY_KEY)).thenReturn("111111");
        when(valueOperations.get(DATA_KEY)).thenReturn(registerRequestJson);

        emailVerificationService.resendCode(resendCodeRequest);

        verify(emailService, times(1)).sendCodeToEmail(eq(LOGIN), anyInt());
        verify(valueOperations, times(1)).set(
                eq(VERIFY_KEY),
                anyString(),
                eq(5L),
                eq(TimeUnit.MINUTES)
        );
        verify(valueOperations, times(1)).set(
                eq(DATA_KEY),
                eq(registerRequestJson),
                eq(5L),
                eq(TimeUnit.MINUTES)
        );
    }

    @Test
    public void resendCode_codeNotFound_throwExc() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(VERIFY_KEY)).thenReturn(null);
        when(valueOperations.get(DATA_KEY)).thenReturn(registerRequestJson);

        var verificationCodeExpiredException = assertThrows(VerificationCodeExpiredException.class, () -> emailVerificationService.resendCode(resendCodeRequest));
        assertEquals("Verification code expired for user with login: " + LOGIN, verificationCodeExpiredException.getMessage());

        verifyNoInteractions(emailService);
    }

    @Test
    public void resendCode_dataNotFound_throwExc() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(VERIFY_KEY)).thenReturn("111111");
        when(valueOperations.get(DATA_KEY)).thenReturn(null);

        var verificationCodeExpiredException = assertThrows(VerificationCodeExpiredException.class, () -> emailVerificationService.resendCode(resendCodeRequest));
        assertEquals("Verification code expired for user with login: " + LOGIN, verificationCodeExpiredException.getMessage());

        verifyNoInteractions(emailService);
    }

}