package com.jobflow.user_service.email;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobflow.user_service.exception.EmailServiceException;
import com.jobflow.user_service.register.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private final EmailService emailService;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String VERIFY_KEY = "email:verify:%s";
    private static final String DATA_KEY = "email:data:%s";
    private static final long TTL_MINUTES = 5L;

    @Override
    public void sendVerificationCode(RegisterRequest registerRequest) {
        String email = registerRequest.getLogin();
        int code = generateSixDigitCode();

        emailService.sendCodeToEmail(email, code);

        String verifyKey = String.format(VERIFY_KEY, email);
        String dataKey = String.format(DATA_KEY, email);

        try {
            String registerRequestJson = objectMapper.writeValueAsString(registerRequest);
            redisTemplate.opsForValue().set(verifyKey, String.valueOf(code), TTL_MINUTES, TimeUnit.MINUTES);
            redisTemplate.opsForValue().set(dataKey, registerRequestJson, TTL_MINUTES, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            throw new EmailServiceException("Failed to serialize register request", e);
        }
    }

    private int generateSixDigitCode() {
        return new Random().nextInt(900000) + 100000;
    }
}
