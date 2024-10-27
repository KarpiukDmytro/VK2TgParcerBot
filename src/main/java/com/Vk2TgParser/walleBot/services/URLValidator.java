package com.Vk2TgParser.walleBot.services;

import lombok.extern.slf4j.Slf4j;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

@Slf4j
public class URLValidator {

    public static boolean isImageURLValid(String imageUrl) {
        return isMediaURLValid(imageUrl, "image");
    }

    public static boolean isVideoURLValid(String videoUrl) {
        return isMediaURLValid(videoUrl, "video");
    }

    public static boolean isMediaURLValid(String mediaUrl, String mediaType) {
        try {
            URI uri = new URI(mediaUrl);
            URL url = uri.toURL();

            // Проверка для видеохостингов (YouTube, Vimeo, VK, RuTube)
            if (mediaType.equals("video") && isVideoHostingURL(mediaUrl)) {
                return true; // Валидный URL для видео с хостингов
            }

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.connect();

            int responseCode = connection.getResponseCode();
            String contentType = connection.getContentType();

            // Проверяем, что ссылка доступна и соответствует указанному типу медиа
            if (responseCode == 200) {
                if ("image".equals(mediaType) && contentType != null && contentType.startsWith("image/")) {
                    return true; // Валидный URL для изображения
                } else if ("video".equals(mediaType) && contentType != null && contentType.startsWith("video/")) {
                    return true; // Валидный URL для видеофайла
                }
            }
        } catch (Exception e) {
            log.warn("Ссылка {} невалидна или не является {}.", mediaUrl, mediaType);
        }
        return false;
    }

    // Метод для проверки, является ли URL видеохостингом (YouTube, Vimeo, VK, RuTube)
    private static boolean isVideoHostingURL(String mediaUrl) {
        return mediaUrl.contains("youtube.com") ||
                mediaUrl.contains("youtu.be") ||
                mediaUrl.contains("vimeo.com") ||
                mediaUrl.contains("vk.com/video") ||
                mediaUrl.contains("rutube.ru");
    }
}