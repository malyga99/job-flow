package com.jobflow.user_service.openId;

import com.jobflow.user_service.user.Role;
import com.jobflow.user_service.user.User;
import com.jobflow.user_service.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class OpenIdUserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenIdUserService.class);

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    public User getOrCreateUser(OpenIdUserInfo userInfo) {
        return userRepository.findByAuthProviderAndAuthProviderId(userInfo.getAuthProvider(), userInfo.getAuthProviderId())
                .orElseGet(() -> {
                    LOGGER.info("Creating new OpenID user with provider id: {}", userInfo.getAuthProviderId());

                    return userRepository.save(User.builder()
                            .firstname(userInfo.getFirstname())
                            .lastname(userInfo.getLastname())
                            .avatar(fetchAvatarFromUrl(userInfo.getAvatarUrl()))
                            .role(Role.ROLE_USER)
                            .authProvider(userInfo.getAuthProvider())
                            .authProviderId(userInfo.getAuthProviderId())
                            .build());
                });
    }

    public byte[] fetchAvatarFromUrl(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }

        ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);
        return response.getBody();
    }
}
