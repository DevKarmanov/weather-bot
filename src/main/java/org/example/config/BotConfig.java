package org.example.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.karmanov.library.service.botCommand.BotHandler;
import dev.karmanov.library.service.botCommand.DefaultBotHandler;
import org.example.config.properties.BotProperties;
import org.example.model.WeatherBot;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(BotProperties.class)
public class BotConfig {
    private final BotProperties botProperties;

    public BotConfig(BotProperties botProperties) {
        this.botProperties = botProperties;
    }

    @Bean
    public WeatherBot weatherBot(){
        return new WeatherBot(botProperties.getName(),botProperties.getToken(),botProperties.getWebHook());
    }

    @Bean
    public BotHandler botHandler(){
        return new DefaultBotHandler();
    }

    @Bean
    public ObjectMapper objectMapper(){return new ObjectMapper();}
}
