package org.example.controller;

import dev.karmanov.library.service.botCommand.BotHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
@RequestMapping("${bot.webHook}")
public class WebHookController {
    private final BotHandler botHandler;

    public WebHookController(BotHandler botHandler) {
        this.botHandler = botHandler;
    }

    @PostMapping
    public void getUpdate(@RequestBody Update update){
        System.out.println(update);
        botHandler.handleMessage(update);
    }
}
