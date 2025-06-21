package com.jobflow.user_service.user;

import com.jobflow.user_service.TestUtil;
import com.jobflow.user_service.exception.InvalidApiKeyException;
import com.jobflow.user_service.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private UserRepository userRepository;

    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    public void setup() {
        SecurityContextHolder.setContext(securityContext);

        userService = new UserServiceImpl(userRepository, "test-key");
        user = TestUtil.createUser();
    }

    @Test
    public void getCurrentUser_returnCurrentUser() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(user);

        User result = userService.getCurrentUser();

        assertNotNull(result);
        assertEquals(user, result);

        verify(authentication, times(1)).isAuthenticated();
        verify(authentication, times(1)).getPrincipal();
    }

    @Test
    public void getCurrentUser_withoutAuthentication_throwExc() {
        when(securityContext.getAuthentication()).thenReturn(null);

        var authenticationException = assertThrows(AuthenticationCredentialsNotFoundException.class, () -> userService.getCurrentUser());
        assertEquals("Current user is not authenticated", authenticationException.getMessage());
    }

    @Test
    public void getCurrentUser_notAuthenticated_throwExc() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        var authenticationException = assertThrows(AuthenticationCredentialsNotFoundException.class, () -> userService.getCurrentUser());
        assertEquals("Current user is not authenticated", authenticationException.getMessage());

        verify(authentication, times(1)).isAuthenticated();
    }

    @Test
    public void getUserInfo_returnUserInfo() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserInfoDto result = userService.getUserInfo(1L, "test-key");

        assertNotNull(result);
        assertEquals(user.getLogin(), result.getEmail());
        assertNull(result.getTelegramChatId());
    }

    @Test
    public void getUserInfo_invalidApiKey_throwExc() {
        var exc = assertThrows(InvalidApiKeyException.class, () -> userService.getUserInfo(1L, "invalid-key"));

        assertEquals("Api key: invalid-key invalid", exc.getMessage());
    }

    @Test
    public void getUserInfo_userNotFound_throwExc() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        var exc = assertThrows(UserNotFoundException.class, () -> userService.getUserInfo(1L, "test-key"));

        assertEquals("User with id: 1 not found", exc.getMessage());
    }

}