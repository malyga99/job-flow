package com.jobflow.notification_service.notification.history;

import com.jobflow.notification_service.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class NotificationHistoryMapperTest {

    @InjectMocks
    private NotificationHistoryMapper notificationHistoryMapper;

    private NotificationHistory notificationHistory;

    @BeforeEach
    public void setup() {
        notificationHistory = TestUtil.createNotificationHistory();
    }
    
    @Test
    public void toDto_returnDto() {
        NotificationHistoryDto result = notificationHistoryMapper.toDto(notificationHistory);

        assertNotNull(result);
        assertEquals(result.getNotificationType(), notificationHistory.getNotificationType());
        assertEquals(result.getSubject(), notificationHistory.getSubject());
        assertEquals(result.getMessage(), notificationHistory.getMessage());
        assertEquals(result.getCreatedAt(), notificationHistory.getCreatedAt());
    }
}