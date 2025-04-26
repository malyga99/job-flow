package com.jobflow.user_service.email;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobflow.user_service.exception.EmailServiceException;
import com.jobflow.user_service.register.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailVerificationServiceImpl.class);

    @Override
    public void sendVerificationCode(RegisterRequest registerRequest) {
        String email = registerRequest.getLogin();
        int code = generateSixDigitCode();
        LOGGER.debug("Generated verification code [{}] for email: {}", code, email);

        String verifyKey = String.format(VERIFY_KEY, email);
        String dataKey = String.format(DATA_KEY, email);

        try {
            String registerRequestJson = objectMapper.writeValueAsString(registerRequest);

            redisTemplate.opsForValue().set(verifyKey, String.valueOf(code), TTL_MINUTES, TimeUnit.MINUTES);
            redisTemplate.opsForValue().set(dataKey, registerRequestJson, TTL_MINUTES, TimeUnit.MINUTES);

            LOGGER.debug("Verification code and registration data successfully stored in Redis for email: {}", email);
        } catch (JsonProcessingException e) {
            throw new EmailServiceException("Failed to serialize register request", e);
        }

        emailService.sendCodeToEmail(email, code);
    }

    private int generateSixDigitCode() {
        return new Random().nextInt(900000) + 100000;
    }
}
