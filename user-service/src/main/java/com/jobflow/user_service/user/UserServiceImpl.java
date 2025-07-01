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

    private final UserRepository userRepository;
    private final String apiKey;

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

        validateApikey(apiKey);

        User user = findByIdOrThrow(userId);

        LOGGER.debug("Successfully fetched user info by id: {}", userId);
        return UserInfoDto.builder()
                .email(user.getLogin())
                .telegramChatId(user.getTelegramChatId())
                .build();
    }

    @Override
    public void linkTelegram(TelegramChatLinkRequest linkRequest, String apiKey) {
        LOGGER.debug("Starting telegram linking for chatId: {} and userId: {}",
                linkRequest.getChatId(), linkRequest.getUserId());

        validateApikey(apiKey);

        Long userId = linkRequest.getUserId();
        Long chatId = linkRequest.getChatId();

        User user = findByIdOrThrow(userId);

        if (user.getTelegramChatId() == null) {
            user.setTelegramChatId(chatId);

            userRepository.save(user);

            LOGGER.debug("Successfully telegram linking for chatId: {} and userId: {}",
                    chatId, userId);
        } else {
            LOGGER.debug("Telegram chat id is already linked with a user with userId: {}", userId);
        }
    }

    private User findByIdOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id: " + userId + " not found"));
    }

    private void validateApikey(String apiKey) {
        if (!this.apiKey.equals(apiKey)) {
            throw new InvalidApiKeyException("Api key: " + apiKey + " invalid");
        }
    }
}
