package com.jobflow.job_tracker_service.notification;

import com.jobflow.job_tracker_service.jobApplication.JobApplication;
import com.jobflow.job_tracker_service.jobApplication.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationEventFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationEventFactory.class);
    private static final String CREATION_SUBJECT = "You have responded to the job application";
    private static final String CREATION_MESSAGE = "You have successfully submitted a response for the %s position to %s";
    private static final String STATUS_UPDATE_MESSAGE = "The status of your %s job application has been updated: %s";
    private static final String STATUS_OFFER_SUBJECT = "Congratulations! You have received an offer";
    private static final String STATUS_OFFER_MESSAGE = "You have received an offer from %s for the position of %s";
    private static final String STATUS_REJECTED_SUBJECT = "You job application has been rejected";
    private static final String STATUS_REJECTED_MESSAGE = "Unfortunately, your job application for the %s position at %s has been rejected";

    public NotificationEvent buildForCreation(JobApplication jobApplication) {
        NotificationEvent notificationEvent = NotificationEvent.builder()
                .userId(jobApplication.getUserId())
                .notificationType(NotificationType.EMAIL)
                .subject(CREATION_SUBJECT)
                .message(String.format(CREATION_MESSAGE, jobApplication.getPosition(), jobApplication.getCompany()))
                .build();

        LOGGER.debug("Created creation notification event for userId: {}", jobApplication.getUserId());
        return notificationEvent;
    }

    public NotificationEvent buildForStatusUpdate(JobApplication jobApplication) {
        NotificationEvent notificationEvent = NotificationEvent.builder()
                .userId(jobApplication.getUserId())
                .build();
        String company = jobApplication.getCompany();
        String position = jobApplication.getPosition();
        Status status = jobApplication.getStatus();

        switch (status) {
            case OFFER -> {
                notificationEvent.setNotificationType(NotificationType.BOTH);
                notificationEvent.setSubject(STATUS_OFFER_SUBJECT);
                notificationEvent.setMessage(String.format(STATUS_OFFER_MESSAGE, company, position));
            }
            case REJECTED -> {
                notificationEvent.setNotificationType(NotificationType.EMAIL);
                notificationEvent.setSubject(STATUS_REJECTED_SUBJECT);
                notificationEvent.setMessage(String.format(STATUS_REJECTED_MESSAGE, position, company));
            }
            default -> {
                notificationEvent.setNotificationType(NotificationType.TELEGRAM);
                notificationEvent.setMessage(String.format(STATUS_UPDATE_MESSAGE, position, status));
            }
        }

        LOGGER.debug("Created status update notification event for userId: {}, status: {}", jobApplication.getUserId(), jobApplication.getStatus());
        return notificationEvent;
    }
}
