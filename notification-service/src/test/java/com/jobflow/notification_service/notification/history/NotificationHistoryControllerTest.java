package com.jobflow.notification_service.notification.history;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jobflow.notification_service.TestUtil;
import com.jobflow.notification_service.handler.GlobalHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class NotificationHistoryControllerTest {

    @Mock
    private NotificationHistoryService historyService;

    @InjectMocks
    private NotificationHistoryController historyController;

    private MockMvc mockMvc;

    private NotificationHistoryDto firstHistoryDto;

    private NotificationHistoryDto secondHistoryDto;

    private Page<NotificationHistoryDto> mockPage;

    @BeforeEach
    public void setup() throws JsonProcessingException {
        mockMvc = MockMvcBuilders.standaloneSetup(historyController)
                .build();

        firstHistoryDto = TestUtil.createNotificationHistoryDto();
        secondHistoryDto = TestUtil.createNotificationHistoryDto();

        mockPage = new PageImpl<>(
                List.of(firstHistoryDto, secondHistoryDto),
                PageRequest.of(0, 10, Sort.by(Direction.DESC, "createdAt")),
                2
        );
    }

    @Test
    public void findMy_returnTwoNotifications() throws Exception {
        var argumentCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(historyService.findMy(any(Pageable.class))).thenReturn(mockPage);

        mockMvc.perform(get("/api/v1/notifications/my")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.content[0].notificationType").value(firstHistoryDto.getNotificationType().toString()))
                .andExpect(jsonPath("$.content[0].subject").value(firstHistoryDto.getSubject()))
                .andExpect(jsonPath("$.content[0].message").value(firstHistoryDto.getMessage()))
                .andExpect(jsonPath("$.content[1].notificationType").value(secondHistoryDto.getNotificationType().toString()))
                .andExpect(jsonPath("$.content[1].subject").value(secondHistoryDto.getSubject()))
                .andExpect(jsonPath("$.content[1].message").value(secondHistoryDto.getMessage()));

        verify(historyService, times(1)).findMy(argumentCaptor.capture());

        Pageable pageable = argumentCaptor.getValue();
        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());

        Sort.Order sortOrder = pageable.getSort().getOrderFor("createdAt");
        assertNotNull(sortOrder);
        assertEquals(Direction.DESC, sortOrder.getDirection());
    }

}