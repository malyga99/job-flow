package com.jobflow.notification_service.telegram;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelegramChatLinkRequest {

    private Long userId;

    private Long chatId;
}
