package com.jobflow.notification_service.notification.history;

import com.jobflow.notification_service.TestUtil;
import com.jobflow.notification_service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationHistoryServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private NotificationHistoryMapper historyMapper;

    @Mock
    private NotificationHistoryRepository historyRepository;

    @InjectMocks
    private NotificationHistoryServiceImpl historyService;

    private NotificationHistory firstHistory;

    private NotificationHistory secondHistory;

    private NotificationHistoryDto firstHistoryDto;

    private NotificationHistoryDto secondHistoryDto;

    private Pageable mockPageable;

    private Page<NotificationHistory> mockPage;

    @BeforeEach
    public void setup() {
        firstHistory = TestUtil.createNotificationHistory();
        secondHistory = TestUtil.createNotificationHistory();

        firstHistoryDto = TestUtil.createNotificationHistoryDto();
        secondHistoryDto = TestUtil.createNotificationHistoryDto();

        mockPageable = PageRequest.of(0, 10);
        mockPage = new PageImpl<>(List.of(firstHistory, secondHistory), mockPageable, 2);
    }

    @Test
    public void findMy_returnTwoNotifications() {
        when(userService.getCurrentUserId()).thenReturn(1L);
        when(historyRepository.findByUserIdAndSuccess(1L, true, mockPageable)).thenReturn(mockPage);
        when(historyMapper.toDto(firstHistory)).thenReturn(firstHistoryDto);
        when(historyMapper.toDto(secondHistory)).thenReturn(secondHistoryDto);

        Page<NotificationHistoryDto> result = historyService.findMy(mockPageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(firstHistoryDto, result.getContent().get(0));
        assertEquals(secondHistoryDto, result.getContent().get(1));

        verify(historyRepository, times(1)).findByUserIdAndSuccess(1L, true, mockPageable);
        verify(historyMapper, times(2)).toDto(any(NotificationHistory.class));
    }
}