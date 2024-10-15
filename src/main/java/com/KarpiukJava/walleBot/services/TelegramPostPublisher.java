package com.KarpiukJava.walleBot.services;

import com.KarpiukJava.walleBot.Configurations.PostData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.bots.DefaultAbsSender;

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
                // Отправляем текст с первым фото
                if (!postData.getPhotoUrls().isEmpty()) {
                    SendPhoto sendPhoto = new SendPhoto();
                    sendPhoto.setChatId(channelId);
                    sendPhoto.setPhoto(new InputFile(postData.getPhotoUrls().getFirst())); // Первое фото
                    sendPhoto.setCaption(postData.getText());  // Текст поста вместе с первым фото
                    telegramBot.execute(sendPhoto);  // Отправляем первое фото и текст
                } else {
                    // Если фото нет, просто отправляем текст
                    SendMessage message = new SendMessage();
                    message.setChatId(channelId);
                    message.setText(postData.getText());
                    telegramBot.execute(message);
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
                }
            } catch (TelegramApiException e) {
                log.error("Ошибка при отправке поста в Telegram", e);
            }
        }
    }
}