package com.jobflow.user_service.email;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobflow.user_service.exception.EmailServiceException;
import com.jobflow.user_service.exception.InvalidVerificationCodeException;
import com.jobflow.user_service.exception.VerificationCodeExpiredException;
import com.jobflow.user_service.register.ConfirmCodeRequest;
import com.jobflow.user_service.register.RegisterRequest;
import com.jobflow.user_service.register.ResendCodeRequest;
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

    private static final String VERIFY_KEY = "email:verify:%s";
    private static final String DATA_KEY = "email:data:%s";
    private static final long TTL_MINUTES = 5L;
    private static final Random RANDOM = new Random();
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailVerificationServiceImpl.class);

    private final EmailService emailService;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void sendVerificationCode(RegisterRequest registerRequest) {
        String email = registerRequest.getLogin();
        int code = generateSixDigitCode();
        LOGGER.debug("Generated verification code [{}] for login: {}", code, email);

        String verifyKey = buildVerifyKey(email);
        String dataKey = buildDataKey(email);

        try {
            String registerRequestJson = objectMapper.writeValueAsString(registerRequest);

            redisTemplate.opsForValue().set(verifyKey, String.valueOf(code), TTL_MINUTES, TimeUnit.MINUTES);
            redisTemplate.opsForValue().set(dataKey, registerRequestJson, TTL_MINUTES, TimeUnit.MINUTES);

            LOGGER.debug("Verification code and registration data successfully stored in Redis for login: {}", email);
        } catch (JsonProcessingException e) {
            throw new EmailServiceException("Failed to serialize register request", e);
        }

        emailService.sendCodeToEmail(email, code);
    }

    @Override
    public RegisterRequest validateVerificationCode(ConfirmCodeRequest confirmCodeRequest) {
        int code = confirmCodeRequest.getCode();
        String email = confirmCodeRequest.getLogin();
        LOGGER.debug("Starting verification code validation for user with login: {}", email);

        String verifyKey = buildVerifyKey(email);
        String dataKey = buildDataKey(email);

        String codeFromRedis = redisTemplate.opsForValue().get(verifyKey);
        String dataFromRedis = redisTemplate.opsForValue().get(dataKey);

        if (codeFromRedis == null || dataFromRedis == null) {
            throw new VerificationCodeExpiredException("Verification code expired for user with login: " + email);
        }
        if (code != Integer.parseInt(codeFromRedis)) {
            throw new InvalidVerificationCodeException("Verification code: " + code + " invalid for user: " + email);
        }

        try {
            RegisterRequest registerRequest = objectMapper.readValue(dataFromRedis, RegisterRequest.class);
            LOGGER.debug("Successfully validated verification code for user with login: {}", email);

            deleteVerificationData(email);
            return registerRequest;
        } catch (JsonProcessingException e) {
            throw new EmailServiceException("Failed to deserialize register request", e);
        }
    }

    @Override
    public void resendCode(ResendCodeRequest resendCodeRequest) {
        String email = resendCodeRequest.getLogin();
        LOGGER.debug("Starting resending code for user with login: {}", email);

        String verifyKey = buildVerifyKey(email);
        String dataKey = buildDataKey(email);

        String codeFromRedis = redisTemplate.opsForValue().get(verifyKey);
        String dataFromRedis = redisTemplate.opsForValue().get(dataKey);

        if (codeFromRedis == null || dataFromRedis == null) {
            throw new VerificationCodeExpiredException("Verification code expired for user with login: " + email);
        }

        int newCode = generateSixDigitCode();
        LOGGER.debug("Generated a new verification code [{}] for login: {}", newCode, email);

        redisTemplate.opsForValue().set(verifyKey, String.valueOf(newCode), TTL_MINUTES, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(dataKey, dataFromRedis, TTL_MINUTES, TimeUnit.MINUTES);

        emailService.sendCodeToEmail(email, newCode);
        LOGGER.debug("Successfully stored in Redis and resent a new verification code for login: {}", email);
    }

    private void deleteVerificationData(String email) {
        redisTemplate.delete(buildVerifyKey(email));
        redisTemplate.delete(buildDataKey(email));
    }

    private int generateSixDigitCode() {
        return RANDOM.nextInt(900000) + 100000;
    }

    private String buildVerifyKey(String email) {
        return String.format(VERIFY_KEY, email);
    }

    private String buildDataKey(String email) {
        return String.format(DATA_KEY, email);
    }
}
