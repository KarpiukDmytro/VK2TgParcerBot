package com.Vk2TgParser.walleBot.Configurations;

import com.Vk2TgParser.walleBot.bot.TelegramBot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BotConfig {

    @Bean
    public TelegramBot telegramBot(@Value("${telegram.botToken}") String botToken,
                                   @Value("${telegram.botUsername}") String botUsername) {
        return new TelegramBot(botToken, botUsername);
    }
}
