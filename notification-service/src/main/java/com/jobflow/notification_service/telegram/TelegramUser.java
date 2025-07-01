package com.jobflow.notification_service.telegram;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelegramUser {

    private Long id;

    private Boolean is_bot;

    private String first_name;
}
