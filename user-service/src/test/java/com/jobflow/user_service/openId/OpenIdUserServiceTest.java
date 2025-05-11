package com.jobflow.user_service.openId;

import com.jobflow.user_service.TestUtil;
import com.jobflow.user_service.user.Role;
import com.jobflow.user_service.user.User;
import com.jobflow.user_service.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpenIdUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RestTemplate restTemplate;

    @Spy
    @InjectMocks
    private OpenIdUserService openIdUserService;

    private OpenIdUserInfo openIdUserInfo;

    private User user;

    @BeforeEach
    public void setup() {
        openIdUserInfo = TestUtil.createOpenIdUserInfo();

        user = TestUtil.createUser();
    }

    @Test
    public void getOrCreateUser_ifExists_returnUserAndDoesNotSave() {
        when(userRepository.findByAuthProviderAndAuthProviderId(openIdUserInfo.getAuthProvider(), openIdUserInfo.getAuthProviderId())).thenReturn(Optional.of(user));

        User result = openIdUserService.getOrCreateUser(openIdUserInfo);

        assertNotNull(result);
        assertEquals(user, result);

        verify(userRepository, times(1)).findByAuthProviderAndAuthProviderId(openIdUserInfo.getAuthProvider(), openIdUserInfo.getAuthProviderId());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void getOrCreateUser_ifDoesNotExists_returnSavedUser() {
        ArgumentCaptor<User> argumentCaptor = ArgumentCaptor.forClass(User.class);

        byte[] mockAvatar = new byte[]{1, 2, 3};
        when(userRepository.findByAuthProviderAndAuthProviderId(openIdUserInfo.getAuthProvider(), openIdUserInfo.getAuthProviderId())).thenReturn(Optional.empty());
        doReturn(mockAvatar).when(openIdUserService).fetchAvatarFromUrl(openIdUserInfo.getAvatarUrl());
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = openIdUserService.getOrCreateUser(openIdUserInfo);

        assertNotNull(result);
        assertEquals(user, result);

        verify(userRepository, times(1)).findByAuthProviderAndAuthProviderId(openIdUserInfo.getAuthProvider(), openIdUserInfo.getAuthProviderId());
        verify(userRepository, times(1)).save(argumentCaptor.capture());

        User savedUser = argumentCaptor.getValue();
        assertEquals(openIdUserInfo.getFirstname(), savedUser.getFirstname());
        assertEquals(openIdUserInfo.getLastname(), savedUser.getLastname());
        assertEquals(openIdUserInfo.getAuthProvider(), savedUser.getAuthProvider());
        assertEquals(openIdUserInfo.getAuthProviderId(), savedUser.getAuthProviderId());
        assertNull(savedUser.getPassword());
        assertEquals(mockAvatar, savedUser.getAvatar());
        assertEquals(Role.ROLE_USER, savedUser.getRole());
    }

    @Test
    public void fetchAvatarFromUrl_returnAvatar() {
        byte[] avatar = new byte[]{1, 2, 3};
        when(restTemplate.getForEntity("test-url", byte[].class)).thenReturn(
                new ResponseEntity<>(avatar, HttpStatus.OK)
        );

        byte[] result = openIdUserService.fetchAvatarFromUrl("test-url");

        assertNotNull(result);
        assertEquals(avatar, result);

        verify(restTemplate, times(1)).getForEntity("test-url", byte[].class);
    }

    @Test
    public void fetchAvatarFromUrl_urlIsNull_returnNull() {
        byte[] result = openIdUserService.fetchAvatarFromUrl(null);

        assertNull(result);
    }

    @Test
    public void fetchAvatarFromUrl_urlIsBlank_returnNull() {
        byte[] result = openIdUserService.fetchAvatarFromUrl("");

        assertNull(result);
    }

}