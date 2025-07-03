package com.jobflow.notification_service.telegram;

import com.jobflow.notification_service.exception.InvalidTelegramTokenException;
import com.jobflow.notification_service.jwt.JwtService;
import com.jobflow.notification_service.user.UserClient;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class TelegramServiceImpl implements TelegramService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramServiceImpl.class);
    private static final String SEND_MESSAGE_URL = "https://api.telegram.org/bot%s/sendMessage";

    private final JwtService jwtService;
    private final UserClient userClient;
    private final TelegramProperties telegramProperties;
    private final RestTemplate restTemplate;

    @Override
    public void processUpdate(TelegramUpdate telegramUpdate, String token) {
        LOGGER.debug("Processing telegram webhook request");

        if (!token.equals(telegramProperties.getBotSecretToken())) {
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

    @Override
    public void send(Long telegramChatId, String message) {
        LOGGER.debug("Sending message to telegram chat ID: {}", telegramChatId);

        Map<String, String> body = Map.of(
                "chat_id", String.valueOf(telegramChatId),
                "text", message
        );
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body);

        String url = String.format(SEND_MESSAGE_URL, telegramProperties.getBotToken());
        restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                Void.class
        );

        LOGGER.debug("Successfully sent message to telegram chat ID: {}", telegramChatId);
    }
}
