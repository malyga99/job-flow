package com.jobflow.user_service.openId;

import com.jobflow.user_service.exception.OpenIdServiceException;
import com.jobflow.user_service.user.Role;
import com.jobflow.user_service.user.User;
import com.jobflow.user_service.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@Service
@RequiredArgsConstructor
public class OpenIdUserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenIdUserService.class);
    private final UserRepository userRepository;

    public User getOrCreateUser(OpenIdUserInfo userInfo) {
        return userRepository.findByLogin(userInfo.getLogin())
                .orElseGet(() -> {
                    LOGGER.info("Creating new OpenID user with login: {}", userInfo.getLogin());
                    return userRepository.save(User.builder()
                            .firstname(userInfo.getFirstname())
                            .lastname(userInfo.getLastname())
                            .login(userInfo.getLogin())
                            .password(null)
                            .avatar(fetchAvatarFromUrl(userInfo.getAvatarUrl()))
                            .role(Role.ROLE_USER)
                            .build());
                });
    }

    public byte[] fetchAvatarFromUrl(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }

        try (InputStream inputStream = new URL(url).openStream()) {
            byte[] avatar = inputStream.readAllBytes();
            LOGGER.debug("Successfully downloaded avatar from URL: {}", url);

            return avatar;
        } catch (IOException e) {
            throw new OpenIdServiceException("Unable to download avatar from external provider: " + e.getMessage(), e);
        }
    }
}
