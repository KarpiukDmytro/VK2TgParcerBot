package com.KarpiukJava.walleBot.bot;

import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    @Value("${telegram.botUsername}")
    private final String botUsername;

    // Используем конструктор для задания токена
    public TelegramBot(String botToken, String botUsername) {
        super(botToken); // Передаём токен в конструктор родительского класса
        this.botUsername = botUsername;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        // Логика обработки обновлений от Telegram
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText("Привет, это автоматический ответ!");

            try {
                execute(message);
            } catch (TelegramApiException e) {
                log.error("Ошибка при отправке сообщения", e);
            }
        }
    }
}
