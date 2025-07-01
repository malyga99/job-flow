package com.jobflow.user_service.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = """
        A dto representing information for linking a user to a telegram chat.
        It is used for micro-service interaction
        """)
public class TelegramChatLinkRequest {

    @Schema(description = "User ID", example = "1")
    private Long userId;

    @Schema(description = "Telegram chat ID", example = "1")
    private Long chatId;
}
