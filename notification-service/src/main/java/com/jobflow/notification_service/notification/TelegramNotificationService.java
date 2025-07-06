package com.jobflow.notification_service.notification;

import com.jobflow.notification_service.telegram.TelegramService;
import com.jobflow.notification_service.user.UserClient;
import com.jobflow.notification_service.user.UserInfo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TelegramNotificationService extends AbstractNotificationService<Long> {

    private final TelegramService telegramService;

    public TelegramNotificationService(
            UserClient userClient,
            NotificationHistoryRepository notificationHistoryRepository,
            TelegramService telegramService) {
        super(userClient, notificationHistoryRepository);
        this.telegramService = telegramService;
    }

    @Override
    protected Long extractContact(UserInfo userInfo) {
        return userInfo.getTelegramChatId();
    }

    @Override
    protected void sendNotification(Long contact, NotificationEvent notificationEvent) {
        telegramService.send(contact, notificationEvent.getMessage());
    }

    @Override
    protected NotificationType getNotificationType() {
        return NotificationType.TELEGRAM;
    }
}
