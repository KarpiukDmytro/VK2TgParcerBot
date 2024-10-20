package com.Vk2TgParser.walleBot.Controllers;

import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimiter {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void sendWithRateLimit(Runnable task, int rateLimit) {
        try {
            task.run();
            TimeUnit.SECONDS.sleep(60 / rateLimit); // Ограничение по времени
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void scheduleTask(Runnable task, int delaySeconds) {
        scheduler.scheduleAtFixedRate(task, 0, delaySeconds, TimeUnit.SECONDS);
    }
}