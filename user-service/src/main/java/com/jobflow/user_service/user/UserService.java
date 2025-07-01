package com.jobflow.user_service.user;

public interface UserService {

    User getCurrentUser();

    UserInfoDto getUserInfo(Long userId, String apiKey);

    void linkTelegram(TelegramChatLinkRequest linkRequest, String apiKey);
}
