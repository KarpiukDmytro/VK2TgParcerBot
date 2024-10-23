package com.Vk2TgParser.walleBot.services;

import lombok.extern.slf4j.Slf4j;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

@Slf4j
public class URLValidator {

    public static boolean isImageURLValid(String imageUrl) {
        try {
            // Преобразуем строку в URI и затем в URL
            URI uri = new URI(imageUrl);
            URL url = uri.toURL();

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.connect();

            int responseCode = connection.getResponseCode();
            String contentType = connection.getContentType();

            // Проверяем, что ссылка доступна и что это изображение
            if (responseCode == 200 && contentType != null && contentType.startsWith("image/")) {
                return true;
            }
        } catch (Exception e) {
            log.warn("Ссылка {} невалидна или не является изображением.", imageUrl);
        }
        return false;
    }
}
