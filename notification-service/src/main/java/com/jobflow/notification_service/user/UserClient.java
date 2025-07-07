package com.jobflow.notification_service.user;

public interface UserClient {

    UserInfo getUserInfo(Long userId);

    void linkChatId(Long userId, Long chatId);
}
