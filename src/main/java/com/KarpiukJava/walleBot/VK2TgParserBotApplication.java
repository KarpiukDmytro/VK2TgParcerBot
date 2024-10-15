package com.KarpiukJava.walleBot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VK2TgParserBotApplication {
    public static void main(String[] args) {
        SpringApplication.run(VK2TgParserBotApplication.class, args);
    }
}