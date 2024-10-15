package com.KarpiukJava.walleBot.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.Optional;

@Service
@Slf4j
public class LastPostIdService {

    private final Path filePath;

    // Получаем путь к файлу из настроек application.yml
    public LastPostIdService(@Value("${filepath.lastPostID}") String filePath) {
        this.filePath = Paths.get(filePath);
        initializeFile(); // Инициализируем файл при создании объекта сервиса
    }

    /**
     * Инициализирует файл lastPostID.txt с начальным значением, если файл не существует.
     */
    private void initializeFile() {
        try {
            if (!Files.exists(filePath)) {
                Files.write(filePath, "0".getBytes(), StandardOpenOption.CREATE);
                log.info("Файл {} создан с начальным значением 0.", filePath);
            }
        } catch (IOException e) {
            log.error("Ошибка при инициализации файла: {}", e.getMessage());
        }
    }

    /**
     * Получает последний сохраненный ID поста.
     *
     * @return Optional<Long> последний ID поста или пусто, если файл пустой или не существует
     */
    public Optional<Long> getLastPostId() {
        try {
            String content = Files.readString(filePath).trim();
            if (content.isEmpty()) {
                return Optional.of(0L);
            }
            return Optional.of(Long.parseLong(content));
        } catch (IOException | NumberFormatException e) {
            log.error("Ошибка при чтении файла: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Обновляет последний сохраненный ID поста.
     *
     * @param lastPostId новый последний ID поста
     */
    public void updateLastPostId(Long lastPostId) {
        try {
            Files.writeString(filePath, lastPostId.toString(), StandardOpenOption.TRUNCATE_EXISTING);
            log.info("Файл {} обновлен. Новый ID: {}", filePath, lastPostId);
        } catch (IOException e) {
            log.error("Ошибка при обновлении файла: {}", e.getMessage());
        }
    }
}
