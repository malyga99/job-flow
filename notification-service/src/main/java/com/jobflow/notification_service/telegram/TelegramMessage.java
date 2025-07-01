package com.jobflow.notification_service.telegram;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelegramMessage {

    private String text;

    private TelegramChat chat;

    private TelegramUser from;
}
