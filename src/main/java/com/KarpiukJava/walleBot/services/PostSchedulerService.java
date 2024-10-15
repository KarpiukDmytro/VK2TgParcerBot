package com.KarpiukJava.walleBot.services;

import com.KarpiukJava.walleBot.Configurations.PostData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class PostSchedulerService {

    private final VKWallPostParser vkWallPostParser;
    private final TelegramPostPublisher telegramPostPublisher;

    public PostSchedulerService(VKWallPostParser vkWallPostParser, TelegramPostPublisher telegramPostPublisher) {
        this.vkWallPostParser = vkWallPostParser;
        this.telegramPostPublisher = telegramPostPublisher;
    }

    // Планируемая задача для регулярной проверки новых постов
    @Scheduled(fixedRateString = "${settings.timeToSleep}", initialDelay = 10000)  // Каждые 10 минут с задержкой старта
    public void checkForNewVKPosts() {
        log.info("Проверяем наличие новых постов...");

        // Парсинг новых постов из ВК
        List<PostData> newPosts = vkWallPostParser.parsePosts();

        // Если есть новые посты, отправляем их в Telegram
        if (newPosts != null && !newPosts.isEmpty()) {
            log.info("Найдено {} новых постов", newPosts.size());
            telegramPostPublisher.sendPostsToTelegram(newPosts);
        } else {
            log.info("Новых постов не найдено");
        }
    }
}