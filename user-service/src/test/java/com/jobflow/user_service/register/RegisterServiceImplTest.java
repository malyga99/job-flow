package com.jobflow.user_service.register;

import com.jobflow.user_service.email.EmailVerificationService;
import com.jobflow.user_service.exception.UserAlreadyExistsException;
import com.jobflow.user_service.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailVerificationService emailVerificationService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegisterServiceImpl registerService;

    private RegisterRequest registerRequest;

    @BeforeEach
    public void setup() {
        registerRequest = new RegisterRequest("Ivan", "Ivanov", "IvanIvanov@gmail.com", "abcde");
    }

    @Test
    public void register_successfullySendCode() {
        when(userRepository.existsByLogin(registerRequest.getLogin().toLowerCase())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        doNothing().when(emailVerificationService).sendVerificationCode(registerRequest);

        registerService.register(registerRequest);

        assertEquals(registerRequest.getLogin().toLowerCase(), registerRequest.getLogin());
        assertEquals("encodedPassword", registerRequest.getPassword());

        verify(emailVerificationService, times(1)).sendVerificationCode(registerRequest);
    }

    @Test
    public void register_userAlreadyExists_throwExc() {
        when(userRepository.existsByLogin(registerRequest.getLogin().toLowerCase())).thenReturn(true);

        var userAlreadyExistsException = assertThrows(UserAlreadyExistsException.class, () -> registerService.register(registerRequest));
        assertEquals("User with login: " + registerRequest.getLogin().toLowerCase() + " already exists", userAlreadyExistsException.getMessage());

        verify(emailVerificationService, never()).sendVerificationCode(registerRequest);
    }

}