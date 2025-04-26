package com.jobflow.user_service.register;

import com.jobflow.user_service.email.EmailService;
import com.jobflow.user_service.email.EmailVerificationService;
import com.jobflow.user_service.exception.UserAlreadyExistsException;
import com.jobflow.user_service.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterServiceImpl implements RegisterService {

    private final UserRepository userRepository;
    private final EmailVerificationService emailVerificationService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void register(RegisterRequest registerRequest) {
        registerRequest.setLogin(registerRequest.getLogin().toLowerCase());

        if (userRepository.existsByLogin(registerRequest.getLogin())) {
            throw new UserAlreadyExistsException("User with login: " + registerRequest.getLogin() + " already exists");
        }

        registerRequest.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        emailVerificationService.sendVerificationCode(registerRequest);
    }
}
