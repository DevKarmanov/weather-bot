package org.example.service.utils;

import org.example.model.WeatherBot;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class BotUtils {
    private final WeatherBot bot;

    public BotUtils(WeatherBot bot) {
        this.bot = bot;
    }

    public void sendMessage(String parseMode, String text, InlineKeyboardMarkup markup,String chatId) {
        executeMessage(getMessage(parseMode,text,markup,chatId));
    }

    public void sendMessage(String text, InlineKeyboardMarkup markup,String chatId) {
        executeMessage(getMessage(null,text,markup,chatId));
    }


    public void sendMessage(String parseMode, String text,String chatId){
        executeMessage(getMessage(parseMode,text,null,chatId));
    }

    public void sendMessage(String parseMode, SendMessage message, InlineKeyboardMarkup markup) {
        message.setReplyMarkup(markup);
        message.setParseMode(parseMode);

        executeMessage(message);
    }

    private SendMessage getMessage(String parseMode, String text, InlineKeyboardMarkup markup,String chatId){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setParseMode(parseMode);
        sendMessage.setReplyMarkup(markup);
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);

        return sendMessage;
    }

    private void executeMessage(SendMessage message){
        try {
            bot.execute(message);
        }catch (TelegramApiException e){
            throw new RuntimeException("Message sending error: "+e.getMessage());
        }

    }


}
