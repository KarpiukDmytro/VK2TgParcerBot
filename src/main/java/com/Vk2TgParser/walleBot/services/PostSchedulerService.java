package com.Vk2TgParser.walleBot.services;

import com.Vk2TgParser.walleBot.Configurations.PostData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class PostSchedulerService {

    private final VKWallPostParser vkWallPostParser;
    private final TelegramPostPublisher telegramPostPublisher;
    private final LastPostIdService lastPostIdService;

    @Value("${request.firstCount}")
    private int firstPostCount;  // Количество постов на первый запуск

    @Value("${request.count}")
    private int postCount;

    @Autowired
    public PostSchedulerService(VKWallPostParser vkWallPostParser,
                                TelegramPostPublisher telegramPostPublisher,
                                LastPostIdService lastPostIdService) {
        this.vkWallPostParser = vkWallPostParser;
        this.telegramPostPublisher = telegramPostPublisher;
        this.lastPostIdService = lastPostIdService;
    }

    // Планируемая задача для регулярной проверки новых постов
    @Scheduled(fixedRateString = "${settings.timeToSleep}", initialDelay = 10000)
    public void checkForNewVKPosts() {
        log.info("Начало проверки новых записей на стене...");

        Optional<Long> optionalLastPostID = lastPostIdService.getLastPostId(); // Получаем последний пост из файла
        Long latestPostID = vkWallPostParser.getLatestPostId(); // Получаем самый свежий пост с ВК

        if (latestPostID == null) {
            log.error("Не удалось получить ID последнего поста.");
            return;
        }

        // Проверяем наличие значения в Optional
        if (optionalLastPostID.isEmpty()) {
            log.error("ID последнего поста не найден в файле.");
            return;
        }

        Long lastPostID = optionalLastPostID.get();
        int difference = Math.toIntExact(latestPostID - lastPostID);
        log.info("Последний пост в файле: {}. Самый свежий пост: {}. Разница: {}",
                lastPostID, latestPostID, difference);

        if (difference >= 5) {
            log.info("Обрабатываем старые посты, начиная с ID: {}", lastPostID);
            processOldPostsRecursively(lastPostID, difference-firstPostCount);  // Запуск рекурсивного метода для старых постов
        } else {
            // Обычная проверка новых постов
            List<PostData> newPosts = vkWallPostParser.parsePostsSince(lastPostID, postCount, 0);

            if (newPosts != null && !newPosts.isEmpty()) {
                log.info("Найдено {} новых постов", newPosts.size());
                telegramPostPublisher.sendPostsToTelegram(newPosts);
                lastPostIdService.updateLastPostId(latestPostID); // Обновляем ID последнего поста
            } else {
                log.info("Новых постов не найдено");
            }
        }
    }

    // Рекурсивный метод для обработки старых постов
    private void processOldPostsRecursively(long lastPostID, int offset) {
        log.info("Обрабатываем старые посты с ID: {} и offset: {}", lastPostID, offset);
        List<PostData> posts = vkWallPostParser.parsePostsSince(lastPostID, firstPostCount, offset);

        if (!posts.isEmpty()) {
            telegramPostPublisher.sendPostsToTelegram(posts);

            // Обновляем lastPostID до ID последнего поста в текущей выборке
            long latestPostId = posts.get(posts.size() - 1).getPostID();
            log.info("Обновляем lastPostID до: {}", latestPostId);
            lastPostIdService.updateLastPostId(latestPostId);  // Записываем новый ID в файл

            // Задержка между запросами для соблюдения ограничения
            try {
                log.info("Задержка перед следующим запросом...");
                Thread.sleep(60000);  // 1 минута
            } catch (InterruptedException e) {
                log.error("Ошибка во время ожидания между отправками постов", e);
                Thread.currentThread().interrupt();
            }

            // Рекурсивный вызов с обновлённым offset
            processOldPostsRecursively(lastPostID, offset + firstPostCount);
        } else {
            log.info("Все старые посты обработаны.");
        }
    }
}
