package com.Vk2TgParser.walleBot.services;

import com.Vk2TgParser.walleBot.Configurations.PostData;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Collections;
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
        Collections.reverse(posts); // Разворачиваем список, чтобы отправлять посты в обратном порядке
        for (PostData postData : posts) {
            try {
                // Проверяем наличие текста
                boolean hasText = postData.getText() != null && !postData.getText().isEmpty();
                List<String> photoUrls = postData.getPhotoUrls();

                // Если текста нет
                if (!hasText) {
                    if (photoUrls.size() == 1) {
                        // Отправляем одно фото без текста
                        sendSinglePhoto(photoUrls.get(0));
                    } else if (photoUrls.size() > 1) {
                        // Отправляем медиа-группу
                        sendMediaGroup(postData, 0);
                    } else {
                        log.warn("Нет текста и фотографий для поста. Пропускаем.");
                    }
                } else {
                    // Отправляем первое фото с текстом (если оно есть)
                    if (!photoUrls.isEmpty()) {
                        SendMessage message = preparingMessageWithSnippet(postData);
                        telegramBot.execute(message);
                        log.info("Отправлено сообщение с текстом и первой ссылкой на фото");
                    } else {
                        // Если фото нет, просто отправляем текст
                        SendMessage message = new SendMessage();
                        message.setChatId(channelId);
                        message.setText(postData.getText());
                        telegramBot.execute(message);
                        log.info("Отправлено сообщение с текстом");
                    }

                    // Если есть дополнительные фото
                    if (photoUrls.size() == 2) {
                        // Отправляем одно оставшееся фото
                        sendSinglePhoto(photoUrls.get(1));
                    } else if (photoUrls.size() > 2) {
                        // Отправляем оставшиеся фотографии как медиа-группу
                        List<InputMedia> media = sendMediaGroup(postData, 1);
                        if (!media.isEmpty()) {
                            SendMediaGroup sendMediaGroup = new SendMediaGroup();
                            sendMediaGroup.setChatId(channelId);
                            sendMediaGroup.setMedias(media);
                            telegramBot.execute(sendMediaGroup);
                            log.info("Отправлена медиа-группа с {} фото", media.size());
                        } else {
                            log.warn("Ни одной валидной ссылки на изображения для отправки.");
                        }
                    }
                }
            } catch (TelegramApiException e) {
                log.error("Ошибка при отправке поста в Telegram", e);
            } catch (Exception e) {
                log.error("Неизвестная ошибка при обработке поста: {}", postData, e);
            }
        }
    }

    @NotNull
    private SendMessage preparingMessageWithSnippet(PostData postData) {
        SendMessage message = new SendMessage();
        message.setChatId(channelId);

        // Добавляем ссылку на первую фотографию в текст сообщения, если она валидная
        String linkToFirstImage =
                (URLValidator.isImageURLValid(postData.getPhotoUrls().get(0)))
                        ? postData.getPhotoUrls().get(0)
                        : "";
        String invisibleLink = "<a href='" + linkToFirstImage + "'>\u200B</a>";

        // Отделяем заголовок
        String[] parts = postData.getText().split("\n", 2); // Указываем 2, чтобы получить максимум 2 части

        // Проверяем, что текст был разделен
        String firstPart = parts.length > 0 ? parts[0] : "";
        String secondPart = parts.length > 1 ? parts[1] : "";

        // Текст поста
        String textWithInvisibleLink = invisibleLink + " \n<b><i>" + firstPart + "</i></b>\n\n" + secondPart;
        message.setText(textWithInvisibleLink);  // Весь текст поста с первой ссылкой на фото
        message.enableHtml(true);
        return message;
    }

    private void sendSinglePhoto(String photoUrl) throws TelegramApiException {
        if (URLValidator.isImageURLValid(photoUrl)) {
            SendPhoto photo = new SendPhoto();
            photo.setChatId(channelId);
            photo.setPhoto(new InputFile(photoUrl)); // Используем InputFile для передачи URL
            telegramBot.execute(photo);
            log.info("Отправлено одиночное фото");
        } else {
            log.warn("Ссылка {} невалидна или не является изображением.", photoUrl);
        }
    }

    @NotNull
    private static List<InputMedia> sendMediaGroup(PostData postData, int firstImage) {
        List<InputMedia> media = new ArrayList<>();

        for (int i = firstImage; i < postData.getPhotoUrls().size(); i++) {
            String photoUrl = postData.getPhotoUrls().get(i);

            if (URLValidator.isImageURLValid(photoUrl)) {
                InputMediaPhoto photo = new InputMediaPhoto(photoUrl);
                media.add(photo);
            } else {
                log.warn("Ссылка {} невалидна или не является изображением.", photoUrl);
            }
        }
        return media;
    }
}
