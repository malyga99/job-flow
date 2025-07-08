package com.jobflow.notification_service.telegram;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A message sent by the bot when the user sends a
 * message. It is used to associate a chat with a user
 * and send notifications.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TelegramUpdate {

    private TelegramMessage message;
}
