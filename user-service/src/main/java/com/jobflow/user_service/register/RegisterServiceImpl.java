package com.jobflow.user_service.register;

import com.jobflow.user_service.email.EmailVerificationService;
import com.jobflow.user_service.exception.UserAlreadyExistsException;
import com.jobflow.user_service.jwt.JwtService;
import com.jobflow.user_service.user.Role;
import com.jobflow.user_service.user.User;
import com.jobflow.user_service.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterServiceImpl implements RegisterService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterServiceImpl.class);

    private final UserRepository userRepository;
    private final EmailVerificationService emailVerificationService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public void register(RegisterRequest registerRequest) {
        LOGGER.debug("Starting user registration with login: {}", registerRequest.getLogin());
        registerRequest.setLogin(setLoginLowercase(registerRequest.getLogin()));

        if (userRepository.existsByLogin(registerRequest.getLogin())) {
            throw new UserAlreadyExistsException("User with login: " + registerRequest.getLogin() + " already exists");
        }

        registerRequest.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        emailVerificationService.sendVerificationCode(registerRequest);
        LOGGER.debug("Successfully sent verification code to user with login: {} for registration", registerRequest.getLogin());
    }

    @Override
    public RegisterResponse confirmCode(ConfirmCodeRequest confirmCodeRequest) {
        LOGGER.debug("Starting confirm code process for user with login: {}", confirmCodeRequest.getLogin());
        confirmCodeRequest.setLogin(setLoginLowercase(confirmCodeRequest.getLogin()));

        RegisterRequest registerRequest = emailVerificationService.validateVerificationCode(confirmCodeRequest);

        User user = userRepository.save(User.builder()
                .firstname(registerRequest.getFirstname())
                .lastname(registerRequest.getLastname())
                .login(registerRequest.getLogin())
                .password(registerRequest.getPassword())
                .role(Role.ROLE_USER)
                .build());

        LOGGER.debug("Successfully confirmed code for user with login: {}", confirmCodeRequest.getLogin());
        return new RegisterResponse(
                jwtService.generateAccessToken(user),
                jwtService.generateRefreshToken(user)
        );
    }

    @Override
    public void resendCode(ResendCodeRequest resendCodeRequest) {
        LOGGER.debug("Starting resend code process for user with login: {}", resendCodeRequest.getLogin());
        resendCodeRequest.setLogin(setLoginLowercase(resendCodeRequest.getLogin()));

        emailVerificationService.resendCode(resendCodeRequest);
        LOGGER.debug("Successfully resent code for user with login: {}", resendCodeRequest.getLogin());
    }

    private String setLoginLowercase(String login) {
        return login.toLowerCase();
    }
}
