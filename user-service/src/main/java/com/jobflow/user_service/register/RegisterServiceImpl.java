package com.jobflow.user_service.register;

import com.jobflow.user_service.email.EmailVerificationService;
import com.jobflow.user_service.exception.FileServiceException;
import com.jobflow.user_service.exception.UserAlreadyExistsException;
import com.jobflow.user_service.jwt.JwtService;
import com.jobflow.user_service.rateLimiter.RateLimiterKeyUtil;
import com.jobflow.user_service.rateLimiter.RateLimiterService;
import com.jobflow.user_service.user.AuthProvider;
import com.jobflow.user_service.user.Role;
import com.jobflow.user_service.user.User;
import com.jobflow.user_service.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RegisterServiceImpl implements RegisterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterServiceImpl.class);

    private final UserRepository userRepository;
    private final EmailVerificationService emailVerificationService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RateLimiterService rateLimiterService;

    @Override
    public void register(RegisterRequest registerRequest, MultipartFile avatar, String clientIp) {
        LOGGER.debug("Starting user registration with login: {}", registerRequest.getLogin());
        registerRequest.setLogin(setLoginLowercase(registerRequest.getLogin()));

        if (userRepository.existsByLogin(registerRequest.getLogin())) {
            throw new UserAlreadyExistsException("User with login: " + registerRequest.getLogin() + " already exists");
        }

        rateLimiterService.validateOrThrow(
                RateLimiterKeyUtil.generateIpKey("register", clientIp),
                10,
                Duration.ofHours(1),
                "Too many register attempts from this IP"
        );

        rateLimiterService.validateOrThrow(
                RateLimiterKeyUtil.generateKey("register", registerRequest.getLogin()),
                5,
                Duration.ofMinutes(1),
                "Too many register attempts. Try again in a minute"
        );

        registerRequest.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        registerRequest.setAvatar(getAvatarBytes(avatar));

        emailVerificationService.sendVerificationCode(registerRequest);
        LOGGER.debug("Successfully sent verification code to user with login: {} for registration", registerRequest.getLogin());
    }

    @Override
    public RegisterResponse confirmCode(ConfirmCodeRequest confirmCodeRequest) {
        LOGGER.debug("Starting confirm code process for user with login: {}", confirmCodeRequest.getLogin());
        confirmCodeRequest.setLogin(setLoginLowercase(confirmCodeRequest.getLogin()));

        rateLimiterService.validateOrThrow(
                RateLimiterKeyUtil.generateKey("confirmCode", confirmCodeRequest.getLogin()),
                5,
                Duration.ofMinutes(1),
                "Too many incorrect code attempts. Try again in a minute"
        );

        RegisterRequest registerRequest = emailVerificationService.validateVerificationCode(confirmCodeRequest);

        User user = userRepository.save(User.builder()
                .firstname(registerRequest.getFirstname())
                .lastname(registerRequest.getLastname())
                .login(registerRequest.getLogin())
                .password(registerRequest.getPassword())
                .avatar(registerRequest.getAvatar())
                .role(Role.ROLE_USER)
                .authProvider(AuthProvider.LOCAL)
                .build());

        LOGGER.debug("Successfully confirmed code for user: {}", user.displayInfo());
        return new RegisterResponse(
                jwtService.generateAccessToken(user),
                jwtService.generateRefreshToken(user)
        );
    }

    @Override
    public void resendCode(ResendCodeRequest resendCodeRequest) {
        LOGGER.debug("Starting resend code process for user with login: {}", resendCodeRequest.getLogin());
        resendCodeRequest.setLogin(setLoginLowercase(resendCodeRequest.getLogin()));

        rateLimiterService.validateOrThrow(
                RateLimiterKeyUtil.generateKey("resendCode", resendCodeRequest.getLogin()),
                5,
                Duration.ofMinutes(1),
                "Too many resend code attempts. Try again in a minute"
        );

        emailVerificationService.resendCode(resendCodeRequest);
        LOGGER.debug("Successfully resent code for user with login: {}", resendCodeRequest.getLogin());
    }

    private String setLoginLowercase(String login) {
        return login.toLowerCase();
    }

    private byte[] getAvatarBytes(MultipartFile avatar) {
        byte[] avatarBytes = null;

        if (avatar != null && !avatar.isEmpty()) {
            try {
                avatarBytes = avatar.getBytes();
            } catch (IOException e) {
                throw new FileServiceException("Failed to get bytes from avatar: " + e.getMessage(), e);
            }
        }

        return avatarBytes;
    }
}
