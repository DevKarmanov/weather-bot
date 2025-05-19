package org.example.model;

import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

public class WeatherBot extends TelegramWebhookBot {
    private final String botName;
    private final String webhookUrl;

    public WeatherBot(String botName, String botToken, String webhookUrl) {
        super(new DefaultBotOptions(),botToken);
        this.botName = botName;
        this.webhookUrl = webhookUrl;
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        return null;
    }

    @Override
    public String getBotPath() {
        return webhookUrl;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }
}
