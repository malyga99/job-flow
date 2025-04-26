package com.jobflow.user_service.email;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobflow.user_service.exception.EmailServiceException;
import com.jobflow.user_service.register.RegisterRequest;
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

    private String registerRequestJson;

    private static final String LOGIN = "IvanIvanov@gmail.com";

    @BeforeEach
    public void setup() {
        registerRequest = new RegisterRequest("Ivan", "Ivanov", LOGIN, "abcde");
        registerRequestJson = "{\"some\":\"json\"}";
    }

    @Test
    public void sendVerificationCode_sendCodeSuccessfullyAndSaveInRedis() throws JsonProcessingException {
        doNothing().when(emailService).sendCodeToEmail(eq(LOGIN), anyInt());
        when(objectMapper.writeValueAsString(registerRequest)).thenReturn(registerRequestJson);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        emailVerificationService.sendVerificationCode(registerRequest);

        verify(emailService, times(1)).sendCodeToEmail(eq(LOGIN), anyInt());
        verify(valueOperations, times(1)).set(
                eq("email:verify:" + LOGIN),
                anyString(),
                eq(5L),
                eq(TimeUnit.MINUTES)
        );
        verify(valueOperations, times(1)).set(
                eq("email:data:" + LOGIN),
                eq(registerRequestJson),
                eq(5L),
                eq(TimeUnit.MINUTES)
        );
    }

    @Test
    public void sendVerificationCode_jsonProcessingException_throwExc() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(registerRequest)).thenThrow( new JsonProcessingException("") {});

        var emailServiceException = assertThrows(EmailServiceException.class, () -> emailVerificationService.sendVerificationCode(registerRequest));
        assertEquals("Failed to serialize register request", emailServiceException.getMessage());

        verifyNoInteractions(redisTemplate, valueOperations, emailService);
    }

}