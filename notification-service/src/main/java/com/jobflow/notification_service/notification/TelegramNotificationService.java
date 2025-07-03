package com.jobflow.notification_service.notification;

import com.jobflow.notification_service.telegram.TelegramService;
import com.jobflow.notification_service.user.UserClient;
import com.jobflow.notification_service.user.UserInfo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TelegramNotificationService implements NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramNotificationService.class);

    private final UserClient userClient;
    private final TelegramService telegramService;

    @Override
    public void send(NotificationEvent notificationEvent) {
        Long userId = notificationEvent.getUserId();
        LOGGER.debug("Sending notification event to user with id: {}", userId);

        UserInfo userInfo = userClient.getUserInfo(userId);
        Long telegramChatId = userInfo.getTelegramChatId();

        if (telegramChatId != null) {
            telegramService.send(telegramChatId, notificationEvent.getMessage());

            LOGGER.debug("Successfully sent notification event to user with id: {}, telegramChatId: {}",
                    userId, telegramChatId);
        } else {
            LOGGER.debug("Telegram chat ID was not found. Notification event not sent to user with id: {}", userId);
        }
    }
}
