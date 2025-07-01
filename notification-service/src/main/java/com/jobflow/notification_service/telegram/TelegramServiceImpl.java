package com.jobflow.notification_service.telegram;

import com.jobflow.notification_service.exception.InvalidTelegramTokenException;
import com.jobflow.notification_service.jwt.JwtService;
import com.jobflow.notification_service.user.UserClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TelegramServiceImpl implements TelegramService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramServiceImpl.class);

    private final JwtService jwtService;
    private final UserClient userClient;
    private final String token;

    public TelegramServiceImpl(JwtService jwtService,
                               UserClient userClient,
                               @Value("${telegram.bot.secret-token}") String token) {
        this.jwtService = jwtService;
        this.userClient = userClient;
        this.token = token;
    }

    @Override
    public void processUpdate(TelegramUpdate telegramUpdate, String token) {
        LOGGER.debug("Processing telegram webhook request");

        if (!this.token.equals(token)) {
            throw new InvalidTelegramTokenException("Token: " + token + " invalid");
        }

        try {
            TelegramMessage message = telegramUpdate.getMessage();
            if (message == null || message.getText() == null) {
                LOGGER.debug("The message was not found. Skip telegram webhook request");
                return;
            }

            String text = message.getText();
            if (text.startsWith("/start ")) {
                String jwtToken = text.substring("/start ".length());
                Long userId = Long.valueOf(jwtService.extractUserId(jwtToken));

                Long chatId = message.getChat().getId();
                userClient.linkChatId(userId, chatId);

                LOGGER.debug("Successfully processed telegram webhook request. chatId: {}, userId: {}", chatId, userId);
            } else {
                LOGGER.debug("The text does not starts with '/start '. Skip telegram webhook request");
            }
        } catch (Exception e) {
            //Not throwing exc because the telegram bot api resends the requests 24 hours until it receives 2xx
            LOGGER.error("Failed to process telegram webhook request: {}", e.getMessage(), e);
        }
    }
}
