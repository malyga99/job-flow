package com.jobflow.user_service.register;

import com.jobflow.user_service.email.EmailVerificationService;
import com.jobflow.user_service.exception.UserAlreadyExistsException;
import com.jobflow.user_service.jwt.JwtService;
import com.jobflow.user_service.user.Role;
import com.jobflow.user_service.user.User;
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

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private RegisterServiceImpl registerService;

    private RegisterRequest registerRequest;

    private ConfirmCodeRequest confirmCodeRequest;

    private ResendCodeRequest resendCodeRequest;

    @BeforeEach
    public void setup() {
        registerRequest = new RegisterRequest("Ivan", "Ivanov", "IvanIvanov@gmail.com", "abcde");
        confirmCodeRequest = new ConfirmCodeRequest("IvanIvanov@gmail.com", 111111);
        resendCodeRequest = new ResendCodeRequest("IvanIvanov@gmail.com");
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

    @Test
    public void confirmCode_returnRequestResponse() {
        User savedUser = new User(1L, "Ivan", "Ivanov", "IvanIvanov@gmail.com", "abcde", Role.ROLE_USER);
        when(emailVerificationService.validateVerificationCode(confirmCodeRequest)).thenReturn(registerRequest);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateAccessToken(savedUser)).thenReturn("access.jwt.token");
        when(jwtService.generateRefreshToken(savedUser)).thenReturn("refresh.jwt.token");

        RegisterResponse result = registerService.confirmCode(confirmCodeRequest);

        assertNotNull(result);
        assertEquals("access.jwt.token", result.getAccessToken());
        assertEquals("refresh.jwt.token", result.getRefreshToken());

        verify(emailVerificationService, times(1)).validateVerificationCode(confirmCodeRequest);
        verify(userRepository, times(1)).save(
                new User(
                        null,
                        registerRequest.getFirstname(),
                        registerRequest.getLastname(),
                        registerRequest.getLogin(),
                        registerRequest.getPassword(),
                        Role.ROLE_USER
                )
        );
    }

    @Test
    public void resendCode_successfullyResendCode() {
        doNothing().when(emailVerificationService).resendCode(resendCodeRequest);

        registerService.resendCode(resendCodeRequest);

        verify(emailVerificationService, times(1)).resendCode(resendCodeRequest);
    }

}