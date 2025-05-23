package org.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.karmanov.library.annotation.userActivity.BotCallBack;
import dev.karmanov.library.annotation.userActivity.BotLocation;
import dev.karmanov.library.annotation.userActivity.BotScheduled;
import dev.karmanov.library.annotation.userActivity.BotText;
import dev.karmanov.library.model.keyboard.InlineKeyboardBuilder;
import dev.karmanov.library.model.user.DefaultUserContext;
import dev.karmanov.library.model.user.UserState;
import dev.karmanov.library.service.notify.Notifier;
import dev.karmanov.library.service.state.StateManager;
import org.example.dto.AI.response.OpenRouterResponse;
import org.example.dto.weather.Root;
import org.example.model.UserEntity;
import org.example.repository.UserRepo;
import org.example.service.api.AIService;
import org.example.service.api.WeatherService;
import org.example.service.utils.BotUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.*;


@Service
public class BotService {
    private static final Logger log = LoggerFactory.getLogger(BotService.class);
    private final Notifier notifier;
    private final BotUtils botUtils;
    private final WeatherService weatherService;
    private final StateManager manager;
    private final AIService aiService;
    private final ObjectMapper mapper;
    private final UserRepo userRepo;

    private final Map<Long,String> userRequests = new HashMap<>();

    private final Map<String, String> cityWeatherCache = new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
            return size() > 100;
        }
    };

    public BotService(Notifier notifier, BotUtils botUtils, WeatherService weatherService, StateManager manager, AIService aiService, ObjectMapper mapper, UserRepo userRepo) {
        this.notifier = notifier;
        this.botUtils = botUtils;
        this.weatherService = weatherService;
        this.manager = manager;
        this.aiService = aiService;
        this.mapper = mapper;
        this.userRepo = userRepo;
    }

    private final static String CITY_INPUT_TEXT = """
        🌤 <b>Укажите город, для которого хотите узнать погоду.</b>

        <b>Допустимые форматы:</b>
        • <code>Город</code> — например: <code>Mogilev</code> \s
        • <code>Город, Страна</code> — например: <code>Mogilev, Belarus</code>

        📍 Вместо текста вы также можете отправить свою геопозицию.

        🌐 Страну можно указывать на русском языке. \s
        Если город не удаётся определить, попробуйте ввести его название на английском.
        """;


    @BotText(text = "/start")
    public void startCommand(Update update){
        String chatId = update.getMessage().getChatId().toString();
        Long userId = update.getMessage().getFrom().getId();

        sendMenu(chatId,userId);
    }

    @BotCallBack(actionName = "weather-search", callbackName = "search")
    public void search(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Long chatId = callbackQuery.getMessage().getChatId();
        Long userId = callbackQuery.getFrom().getId();

        InlineKeyboardMarkup markup = new InlineKeyboardBuilder()
                .button("Хватит","cancel")
                .build();

        botUtils.sendMessage("HTML",CITY_INPUT_TEXT,markup, String.valueOf(chatId));

        manager.setNextStep(userId,DefaultUserContext.builder()
                        .addState(Set.of(UserState.AWAITING_TEXT,UserState.AWAITING_CALLBACK,UserState.AWAITING_LOCATION))
                        .addActionData(Set.of("city-wait","cancel-button","start-command-method"))
                .build());
    }

    @BotCallBack(actionName = "cancel-button",callbackName = "cancel")
    public void cancel(Update update){
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Long chatId = callbackQuery.getMessage().getChatId();
        Long userId = callbackQuery.getFrom().getId();

        userRequests.remove(userId);

        sendMenu(String.valueOf(chatId),userId);
    }

    private void sendMenu(String chatId,Long userId){
        InlineKeyboardMarkup markup = new InlineKeyboardBuilder()
                .button("Узнать погоду конкретного города","search")
                .newRow()
                .button("Установить город по умолчанию","default-city")
                .build();

        String messageText = "Привет! Что хочешь сделать?";

        botUtils.sendMessage(messageText,markup,chatId);

        setDefaultMenuStates(userId);
    }

    private void setDefaultMenuStates(Long userId){
        manager.setNextStep(userId, DefaultUserContext
                .builder()
                .addActionData(Set.of("weather-search","default-city-button","start-command-method"))
                .addState(Set.of(UserState.AWAITING_CALLBACK,UserState.AWAITING_TEXT))
                .build());
    }

    @BotCallBack(actionName = "default-city-button",callbackName = "default-city")
    @Transactional
    public void defaultCity(Update update){
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Long chatId = callbackQuery.getMessage().getChatId();
        Long userId = callbackQuery.getFrom().getId();

        InlineKeyboardMarkup markup = new InlineKeyboardBuilder()
                .button("Передумал","cancel")
                .newRow()
                .button("Сбросить","fail").build();

        String currentCityText = "Текущий город по умолчанию: ";
        String city = userRepo.getReferenceById(userId).getDefaultCity();
        currentCityText+=city==null?"не установлен":city;

        botUtils.sendMessage("HTML",currentCityText, String.valueOf(chatId));

        botUtils.sendMessage("HTML",CITY_INPUT_TEXT,markup, String.valueOf(chatId));

        manager.setNextStep(userId,DefaultUserContext.builder()
                        .addState(Set.of(UserState.AWAITING_TEXT,UserState.AWAITING_CALLBACK,UserState.AWAITING_LOCATION))
                        .addActionData(Set.of("set-default-city","cancel-button","fail-option","start-command-method"))
                .build());
    }

    @BotText(actionName = "set-default-city",text = "(?i)^(?:[a-zа-яё]+(?:\\s[a-zа-яё]+)*)(?:,\\s*[a-zа-яё]+(?:\\s[a-zа-яё]+)*)*$",isRegex = true)
    @Transactional
    public void setDefaultCity(Update update) throws JsonProcessingException {
        String chatId = update.getMessage().getChatId().toString();
        Long userId = update.getMessage().getFrom().getId();
        String text = update.getMessage().getText();

        setCity(userId,chatId,text);
    }

    @BotLocation(actionName = "set-default-city")
    @Transactional
    public void setDefaultCityCoords(Update update) throws JsonProcessingException {
        Message message = update.getMessage();
        Long userId = message.getFrom().getId();
        String chatId = message.getChatId().toString();

        Location location = message.getLocation();
        String coords = location.getLatitude() + "," + location.getLongitude();
        log.debug("User coordinates: {}",coords);

        setCity(userId,chatId,coords);
    }

    private void setCity(Long userId,String chatId,String city) throws JsonProcessingException {
        UserEntity user = userRepo.getReferenceById(userId);
        user.addRole("have-default-city");
        user.setDefaultCity(city.toLowerCase(Locale.ROOT).trim());
        user.setChatId(chatId);
        userRepo.saveAndFlush(user);

        InlineKeyboardMarkup markup = new InlineKeyboardBuilder()
                .button("Подтвердить","success")
                .newRow()
                .button("Отмена","fail")
                .build();

        botUtils.sendMessage("HTML",weatherService.getWeatherWithForecast(city, Long.valueOf(chatId),false,true,true),markup);

        manager.setNextStep(userId,DefaultUserContext.builder()
                .addActionData(Set.of("success-option","fail-option"))
                .addState(UserState.AWAITING_CALLBACK)
                .build());
    }

    @BotCallBack(actionName = "success-option",callbackName = "success")
    public void success(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Long chatId = callbackQuery.getMessage().getChatId();
        Long userId = callbackQuery.getFrom().getId();

        notifier.sendMessage(chatId,"Теперь бы будете получать ежедневную рассылку погоды по этому городу");

        sendMenu(String.valueOf(chatId),userId);
    }

    @BotCallBack(actionName = "fail-option",callbackName = "fail")
    @Transactional
    public void failed(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Long chatId = callbackQuery.getMessage().getChatId();
        Long userId = callbackQuery.getFrom().getId();

        UserEntity user = userRepo.getReferenceById(userId);

        if (user.getDefaultCity()==null){
            notifier.sendMessage(chatId,"Отменять нечего");
        }else {
            user.setDefaultCity(null);
            user.removeRole("have-default-city");
            user.setChatId(null);
            userRepo.saveAndFlush(user);

            notifier.sendMessage(chatId,"Вы всё отменили");
        }


        sendMenu(String.valueOf(chatId),userId);
    }



    @BotText(actionName = "city-wait",text = "(?i)^(?:[a-zа-яё]+(?:\\s[a-zа-яё]+)*)(?:,\\s*[a-zа-яё]+(?:\\s[a-zа-яё]+)*)*$",isRegex = true)
    public void cityWait(Update update) {
        Long chatId = update.getMessage().getChatId();
        Long userId = update.getMessage().getFrom().getId();
        String text = update.getMessage().getText();

       sendAdvice(userId,text,chatId,true,true,false);
    }

    private void sendAdvice(Long userId,String text,Long chatId,boolean askAI,boolean useCache,boolean refreshCache){
        manager.setNextStep(userId,DefaultUserContext
                .builder()
                .addState(UserState.DEFAULT)
                .build());

        try {
            InlineKeyboardMarkup markup = new InlineKeyboardBuilder()
                    .button("Хватит","cancel")
                    .newRow()
                    .button("Перегенерировать","regenerate")
                    .build();

            SendMessage message = weatherService.getWeatherWithForecast(text,chatId,askAI,useCache,refreshCache);
            message.setReplyMarkup(markup);
            botUtils.sendMessage("HTML",message,markup);

            userRequests.put(userId,text);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } finally {
            manager.setNextStep(userId, DefaultUserContext
                    .builder()
                    .addActionData(Set.of("city-wait","cancel-button","regenerate","start-command-method"))
                    .addState(Set.of(UserState.AWAITING_TEXT,UserState.AWAITING_CALLBACK,UserState.AWAITING_LOCATION))
                    .build());
        }
    }


    @BotCallBack(actionName = "regenerate",callbackName = "regenerate")
    public void regenerate(Update update){
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Long chatId = callbackQuery.getMessage().getChatId();
        Long userId =callbackQuery.getFrom().getId();

        String userPreviousText = userRequests.get(userId);
        if (userPreviousText == null || userPreviousText.isEmpty()){
            notifier.sendMessage(chatId,"Извините, сервер был перезагружен и ваш предыдущий запрос стёрся.\nОтправьте новый!");
        }else {
            sendAdvice(userId,userRequests.get(userId),chatId,true,false,true);
        }
    }

    @BotScheduled(cron = "0 0 9-23/2 * * ?", zone = "Europe/Minsk", roles = "have-default-city")
    public void sendWeather(Set<Long> selectedUserIds) {
        userRepo.findAllByUserIdIn(selectedUserIds).forEach(user -> {
            if (user.getChatId() == null) {
                log.debug("User: {} has chatId=null",user);
                return;
            }

            String city = user.getDefaultCity();

            if (cityWeatherCache.containsKey(city)){
                botUtils.sendMessage("HTML",cityWeatherCache.get(city),user.getChatId());
            }else {
                try {
                    String forecast = weatherService.getWeather(city);

                    OpenRouterResponse AIResponse = aiService.getAdvice(forecast,false,true);

                    String weatherInfo = weatherService.formatWeatherInformation(mapper.readValue(forecast, Root.class));

                    String fullInfo = "Рассылка погодных новостей!\n"+weatherInfo+"\n"+AIResponse.getChoices().get(0).getMessage().getContent();

                    InlineKeyboardMarkup markup = new InlineKeyboardBuilder()
                            .button("Вызвать меню","menu")
                            .build();
                    botUtils.sendMessage("HTML",fullInfo,markup,user.getChatId());
                    log.debug("send message to user: {}",user);
                    cityWeatherCache.put(city,fullInfo);

                    manager.setNextStep(user.getUserId(),DefaultUserContext.builder()
                                    .addState(Set.of(UserState.AWAITING_CALLBACK))
                                    .addActionData(Set.of("menu-button","start-command-method"))
                            .build());
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }

        });
        cityWeatherCache.clear();
    }

    @BotCallBack(actionName = "menu-button",callbackName = "menu")
    public void menu(Update update){
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Long chatId = callbackQuery.getMessage().getChatId();
        Long userId = callbackQuery.getFrom().getId();

        sendMenu(String.valueOf(chatId),userId);
    }

    @BotLocation(actionName = "city-wait")
    public void setCityCoords(Update update){
        Message message = update.getMessage();
        Long userId = message.getFrom().getId();
        Long chatId = message.getChatId();

        Location location = message.getLocation();
        String coords = location.getLatitude() + "," + location.getLongitude();
        log.debug("User coordinates: {}",coords);

        sendAdvice(userId,coords,chatId,true,true,false);

    }


}
