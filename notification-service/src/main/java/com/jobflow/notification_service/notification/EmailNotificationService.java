package com.jobflow.notification_service.notification;

import com.jobflow.notification_service.email.EmailService;
import com.jobflow.notification_service.user.UserClient;
import com.jobflow.notification_service.user.UserInfo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailNotificationService implements NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailNotificationService.class);

    private final UserClient userClient;
    private final EmailService emailService;

    @Override
    public void send(NotificationEvent notificationEvent) {
        LOGGER.debug("Sending notification event to user with id: {}", notificationEvent.getUserId());

        UserInfo userInfo = userClient.getUserInfo(notificationEvent.getUserId());
        String email = userInfo.getEmail();

        if (email != null) {
            emailService.send(
                    email,
                    notificationEvent.getSubject(),
                    notificationEvent.getMessage()
            );

            LOGGER.debug("Successfully sent notification event to user with id: {}, email: {}",
                    notificationEvent.getUserId(), email);
        } else {
            LOGGER.debug("Email was not found. Notification event not sent to user with id: {}", notificationEvent.getUserId());
        }
    }
}
