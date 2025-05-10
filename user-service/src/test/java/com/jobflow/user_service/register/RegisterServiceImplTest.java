package com.jobflow.user_service.register;

import com.jobflow.user_service.TestUtil;
import com.jobflow.user_service.email.EmailVerificationService;
import com.jobflow.user_service.exception.FileServiceException;
import com.jobflow.user_service.exception.UserAlreadyExistsException;
import com.jobflow.user_service.jwt.JwtService;
import com.jobflow.user_service.user.AuthProvider;
import com.jobflow.user_service.user.Role;
import com.jobflow.user_service.user.User;
import com.jobflow.user_service.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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

    private MultipartFile avatar;

    @BeforeEach
    public void setup() {
        registerRequest = TestUtil.createRegisterRequest();

        confirmCodeRequest = TestUtil.createConfirmCodeRequest();

        resendCodeRequest = TestUtil.createResendCodeRequest();

        avatar = new MockMultipartFile("avatar", "dummy".getBytes());
    }

    @Test
    public void register_successfullySendCode() {
        when(userRepository.existsByLogin(registerRequest.getLogin().toLowerCase())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        doNothing().when(emailVerificationService).sendVerificationCode(registerRequest);

        registerService.register(registerRequest, null);

        assertEquals(registerRequest.getLogin().toLowerCase(), registerRequest.getLogin());
        assertEquals("encodedPassword", registerRequest.getPassword());
        assertNull(registerRequest.getAvatar());

        verify(emailVerificationService, times(1)).sendVerificationCode(registerRequest);
    }

    @Test
    public void register_withAvatar_setAvatarCorrectly() {
        when(userRepository.existsByLogin(registerRequest.getLogin().toLowerCase())).thenReturn(false);

        registerService.register(registerRequest, avatar);

        assertNotNull(registerRequest.getAvatar());
    }

    @Test
    public void register_avatarIsEmpty_doesNotSetAvatar() {
        MultipartFile emptyAvatar = new MockMultipartFile("avatar", "".getBytes());
        when(userRepository.existsByLogin(registerRequest.getLogin().toLowerCase())).thenReturn(false);

        registerService.register(registerRequest, emptyAvatar);

        assertNull(registerRequest.getAvatar());
    }

    @Test
    public void register_userAlreadyExists_throwExc() {
        when(userRepository.existsByLogin(registerRequest.getLogin().toLowerCase())).thenReturn(true);

        var userAlreadyExistsException = assertThrows(UserAlreadyExistsException.class, () -> registerService.register(registerRequest, null));
        assertEquals("User with login: " + registerRequest.getLogin().toLowerCase() + " already exists", userAlreadyExistsException.getMessage());

        verify(emailVerificationService, never()).sendVerificationCode(registerRequest);
    }

    @Test
    public void register_failedToGetBytesFromAvatar_throwExc() throws IOException {
        IOException ioException = new IOException("IO Exception");
        MultipartFile mockMultipartFile = mock(MultipartFile.class);
        when(userRepository.existsByLogin(registerRequest.getLogin().toLowerCase())).thenReturn(false);
        when(mockMultipartFile.getBytes()).thenThrow(ioException);

        var fileServiceException = assertThrows(FileServiceException.class, () -> registerService.register(registerRequest, mockMultipartFile));
        assertEquals("Failed to get bytes from avatar: " + ioException.getMessage(), fileServiceException.getMessage());

        verifyNoInteractions(emailVerificationService);
    }

    @Test
    public void confirmCode_returnRegisterResponse() {
        User savedUser = User.builder()
                .id(1L)
                .firstname("Ivan")
                .lastname("Ivanov")
                .login("IvanIvanov@gmail.com")
                .password("abcde")
                .role(Role.ROLE_USER)
                .build();
        when(emailVerificationService.validateVerificationCode(confirmCodeRequest)).thenReturn(registerRequest);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateAccessToken(savedUser)).thenReturn(TestUtil.ACCESS_TOKEN);
        when(jwtService.generateRefreshToken(savedUser)).thenReturn(TestUtil.REFRESH_TOKEN);

        RegisterResponse result = registerService.confirmCode(confirmCodeRequest);

        assertNotNull(result);
        assertEquals(TestUtil.ACCESS_TOKEN, result.getAccessToken());
        assertEquals(TestUtil.REFRESH_TOKEN, result.getRefreshToken());

        verify(emailVerificationService, times(1)).validateVerificationCode(confirmCodeRequest);
        verify(userRepository, times(1)).save(
                User.builder()
                        .firstname(registerRequest.getFirstname())
                        .lastname(registerRequest.getLastname())
                        .login(registerRequest.getLogin())
                        .password(registerRequest.getPassword())
                        .avatar(registerRequest.getAvatar())
                        .role(Role.ROLE_USER)
                        .authProvider(AuthProvider.LOCAL)
                        .build());
    }

    @Test
    public void resendCode_successfullyResendCode() {
        doNothing().when(emailVerificationService).resendCode(resendCodeRequest);

        registerService.resendCode(resendCodeRequest);

        verify(emailVerificationService, times(1)).resendCode(resendCodeRequest);
    }

}