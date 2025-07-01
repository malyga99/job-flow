package com.jobflow.notification_service.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A dto representing information about the user. It is used for micro-service interaction")
public class UserInfo {

    @Schema(description = "User email", example = "IvanIvanov@gmail.com")
    private String email;

    @Schema(description = "Telegram chat ID", example = "123")
    private Long telegramChatId;
}
