package org.example.service.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.karmanov.library.service.notify.Notifier;
import org.example.dto.weather.Forecastday;
import org.example.dto.weather.Hour;
import org.example.dto.weather.Location;
import org.example.dto.weather.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class WeatherService {

    private static final Logger log = LoggerFactory.getLogger(AIService.class);

    @Value("${api.weather-api-key}")
    private String WEATHER_API_KEY;
    private final Notifier notifier;
    private final AIService aiService;
    private final ApiService apiService;
    private final ObjectMapper mapper;

    public WeatherService(Notifier notifier, AIService aiService, ApiService apiService, ObjectMapper mapper) {
        this.notifier = notifier;
        this.aiService = aiService;
        this.apiService = apiService;
        this.mapper = mapper;
    }

    public String formatWeatherInformation(Root root){
        Location location = root.getLocation();
        Forecastday forecastday = root.getForecast().getForecastday().get(0);

        int hour = LocalDateTime.now().getHour();

        Hour currHour = forecastday.getHour().get(hour);
        log.debug("All hours: {}",currHour);
        log.debug("Get hour num {}:{}",hour,forecastday.getHour().get(hour));

        double currentTemp = currHour.getTemp_c();
        String currentCondition = currHour.getCondition().getText();

        Hour futeHour = forecastday.getHour().get(hour+2);
        double tempAfter2 = futeHour.getTemp_c();
        String conditionAfter2 = futeHour.getCondition().getText();

        String city = String.format("🌍 Город: %s (%s)%n", location.getName(), location.getCountry());
        String temperature = String.format("🌡 Температура сейчас: %.1f°C%n", currentTemp);
        String state = String.format("☁️ Состояние сейчас: %s%n", currentCondition);
        String forecastText = String.format("⏳ Через 2 часа: %.1f°C, %s%n", tempAfter2, conditionAfter2);

        return city + temperature + state + forecastText;
    }

    public String getWeather(String city){
        Map<String,String> queryParams =  Map.of(
                "key",WEATHER_API_KEY,
                "q",city,
                "aqi","no",
        "lang","ru");

        return getWeather(queryParams);
    }

    private String getWeather(Map<String,String> queryParams){

        CompletableFuture<String> forecastWeatherFuture = CompletableFuture.supplyAsync(() ->
                apiService.getRequest("https", "api.weatherapi.com", "/v1/forecast.json", queryParams, String.class));

        return forecastWeatherFuture.join();
    }

    public SendMessage getWeatherWithForecast(String text, Long chatId, boolean askAI,boolean useCache,boolean refreshCache) throws JsonProcessingException {
        notifier.sendMessage(chatId, "Узнаю прогноз погоды...");

        return getForecast(getWeather(text),chatId,askAI,useCache,refreshCache);

    }


    private SendMessage getForecast(String forecast,Long chatId,boolean askAI,boolean useCache,boolean refreshCache) throws JsonProcessingException {
        Root root = mapper.readValue(forecast, Root.class);

        String fullMessage = formatWeatherInformation(root);

        if (!askAI) {
            return new SendMessage(String.valueOf(chatId), fullMessage);
        }


        notifier.sendMessage(chatId, fullMessage);
        notifier.sendMessage(chatId, "Попросим рекомендации у ИИ...");

        try {
            return new SendMessage(String.valueOf(chatId),
                    aiService.getAdvice(forecast,useCache,refreshCache).getChoices().get(0).getMessage().getContent());
        }catch (Exception e){
            log.error("Ошибка при обращении к ИИ", e);
            return new SendMessage(String.valueOf(chatId),"Ошибка обращения к ИИ");
        }
    }
}
