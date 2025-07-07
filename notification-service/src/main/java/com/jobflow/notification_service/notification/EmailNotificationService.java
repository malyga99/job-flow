package com.jobflow.notification_service.notification;

import com.jobflow.notification_service.email.EmailService;
import com.jobflow.notification_service.notification.history.NotificationHistoryRepository;
import com.jobflow.notification_service.user.UserClient;
import com.jobflow.notification_service.user.UserInfo;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService extends AbstractNotificationService<String> {

    private final EmailService emailService;

    public EmailNotificationService(
            UserClient userClient,
            NotificationHistoryRepository notificationHistoryRepository,
            EmailService emailService) {
        super(userClient, notificationHistoryRepository);
        this.emailService = emailService;
    }

    @Override
    protected String extractContact(UserInfo userInfo) {
        return userInfo.getEmail();
    }

    @Override
    protected void sendNotification(String contact, NotificationEvent notificationEvent) {
        emailService.send(
                contact,
                notificationEvent.getSubject(),
                notificationEvent.getMessage()
        );
    }

    @Override
    protected NotificationType getNotificationType() {
        return NotificationType.EMAIL;
    }
}
