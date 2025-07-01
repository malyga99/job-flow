package com.jobflow.notification_service.telegram;

public interface TelegramService {

    void processUpdate(TelegramUpdate telegramUpdate, String token);
}
