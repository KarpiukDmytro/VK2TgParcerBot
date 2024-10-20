package com.Vk2TgParser.walleBot.services;

import com.Vk2TgParser.walleBot.Configurations.PostData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class TelegramPostPublisher {

    private final DefaultAbsSender telegramBot;

    @Value("${telegram.channel}")
    private String channelId;

    public TelegramPostPublisher(DefaultAbsSender telegramBot) {
        this.telegramBot = telegramBot;
    }

    public void sendPostsToTelegram(List<PostData> posts) {
        for (PostData postData : posts) {
            try {
                // Отправляем первое сообщение с текстом и ссылкой на первое фото
                log.info("Отправляем текст с ссылкой на первое фото: {}", postData.getText());
                if (!postData.getPhotoUrls().isEmpty()) {
                    SendMessage message = new SendMessage();
                    message.setChatId(channelId);
                    String textWithLink = postData.getText() + "\n\nСсылка на фото: " + postData.getPhotoUrls().get(0);
                    message.setText(textWithLink);  // Текст поста с первой ссылкой на фото
                    telegramBot.execute(message);   // Отправляем текст с ссылкой на фото
                    log.info("Отправлено сообщение с текстом и ссылкой на фото: {}", postData.getPhotoUrls().get(0));
                } else {
                    // Если фото нет, просто отправляем текст
                    SendMessage message = new SendMessage();
                    message.setChatId(channelId);
                    message.setText(postData.getText());
                    telegramBot.execute(message);
                    log.info("Отправлено сообщение с текстом");
                }

                // Отправляем остальные фотографии как медиа группу
                if (postData.getPhotoUrls().size() > 1) {
                    List<InputMedia> media = new ArrayList<>();

                    for (int i = 1; i < postData.getPhotoUrls().size(); i++) {
                        InputMediaPhoto photo = new InputMediaPhoto(postData.getPhotoUrls().get(i));
                        media.add(photo);
                    }

                    SendMediaGroup sendMediaGroup = new SendMediaGroup();
                    sendMediaGroup.setChatId(channelId);
                    sendMediaGroup.setMedias(media);
                    telegramBot.execute(sendMediaGroup);  // Отправляем медиа группу с оставшимися фото
                    log.info("Отправлена медиа-группа с {} фото", postData.getPhotoUrls().size() - 1);
                }
            } catch (TelegramApiException e) {
                log.error("Ошибка при отправке поста в Telegram", e);
            }
        }
    }
}
