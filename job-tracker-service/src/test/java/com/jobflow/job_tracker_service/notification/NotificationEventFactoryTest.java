package com.jobflow.job_tracker_service.notification;

import com.jobflow.job_tracker_service.TestUtil;
import com.jobflow.job_tracker_service.jobApplication.JobApplication;
import com.jobflow.job_tracker_service.jobApplication.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class NotificationEventFactoryTest {

    @InjectMocks
    private NotificationEventFactory eventFactory;

    private JobApplication jobApplication;

    @BeforeEach
    public void setup() {
        jobApplication = TestUtil.createJobApplication();
    }

    @Test
    public void buildForCreation_returnCorrectlyEvent() {
        NotificationEvent result = eventFactory.buildForCreation(jobApplication);

        assertNotNull(result);
        assertEquals(jobApplication.getUserId(), result.getUserId());
        assertEquals(NotificationType.EMAIL, result.getNotificationType());
        assertEquals("You have responded to the job application", result.getSubject());
        assertEquals(String.format("You have successfully submitted a response for the %s position to %s",
                jobApplication.getPosition(), jobApplication.getCompany()), result.getMessage());
    }

    @Test
    public void buildForUpdate_statusIsOffer_returnCorrectlyEvent() {
        jobApplication.setStatus(Status.OFFER);

        NotificationEvent result = eventFactory.buildForStatusUpdate(jobApplication);

        assertNotNull(result);
        assertEquals(jobApplication.getUserId(), result.getUserId());
        assertEquals(NotificationType.BOTH, result.getNotificationType());
        assertEquals("Congratulations! You have received an offer", result.getSubject());
        assertEquals(String.format("You have received an offer from %s for the position of %s",
                jobApplication.getCompany(), jobApplication.getPosition()), result.getMessage());
    }

    @Test
    public void buildForUpdate_statusIsRejected_returnCorrectlyEvent() {
        jobApplication.setStatus(Status.REJECTED);

        NotificationEvent result = eventFactory.buildForStatusUpdate(jobApplication);

        assertNotNull(result);
        assertEquals(jobApplication.getUserId(), result.getUserId());
        assertEquals(NotificationType.EMAIL, result.getNotificationType());
        assertEquals("You job application has been rejected", result.getSubject());
        assertEquals(String.format("Unfortunately, your job application for the %s position at %s has been rejected",
                jobApplication.getPosition(), jobApplication.getCompany()), result.getMessage());
    }

    @Test
    public void buildForUpdate_otherStatus_returnCorrectlyEvent() {
        jobApplication.setStatus(Status.APPLIED);

        NotificationEvent result = eventFactory.buildForStatusUpdate(jobApplication);

        assertNotNull(result);
        assertEquals(jobApplication.getUserId(), result.getUserId());
        assertEquals(NotificationType.TELEGRAM, result.getNotificationType());
        assertNull(result.getSubject());
        assertEquals(String.format("The status of your %s job application has been updated: %s",
                jobApplication.getPosition(), jobApplication.getStatus()), result.getMessage());
    }



}