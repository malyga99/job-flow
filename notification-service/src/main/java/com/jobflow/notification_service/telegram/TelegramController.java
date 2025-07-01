package com.jobflow.notification_service.telegram;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/telegram")
@RequiredArgsConstructor
public class TelegramController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramController.class);

    private final TelegramService telegramService;

    @PostMapping("/webhook")
    public ResponseEntity<Void> receiveUpdate(
            @RequestHeader("X-Telegram-Bot-Api-Secret-Token") String token,
            @RequestBody TelegramUpdate telegramUpdate
    ) {
        LOGGER.info("[POST] Telegram webhook request received");
        telegramService.processUpdate(telegramUpdate, token);

        return ResponseEntity.ok().build();
    }
}
