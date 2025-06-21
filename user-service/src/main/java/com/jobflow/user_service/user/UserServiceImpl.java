package com.jobflow.user_service.user;

import com.jobflow.user_service.exception.InvalidApiKeyException;
import com.jobflow.user_service.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    private UserRepository userRepository;

    private String apiKey;

    public UserServiceImpl(UserRepository userRepository,
                           @Value("${notification.service.api-key}") String apiKey) {
        this.userRepository = userRepository;
        this.apiKey = apiKey;
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationCredentialsNotFoundException("Current user is not authenticated");
        }

        User currentUser = (User) authentication.getPrincipal();
        LOGGER.debug("Fetched current user: {}", currentUser.displayInfo());

        return currentUser;
    }

    @Override
    public UserInfoDto getUserInfo(Long userId, String apiKey) {
        LOGGER.debug("Fetching user info by id: {}", userId);

        if (!this.apiKey.equals(apiKey)) {
            throw new InvalidApiKeyException("Api key: " + apiKey + " invalid");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id: " + userId + " not found"));

        LOGGER.debug("Successfully fetched user info by id: {}", userId);
        return UserInfoDto.builder()
                .email(user.getLogin())
                .telegramChatId(null)
                .build();
    }
}
