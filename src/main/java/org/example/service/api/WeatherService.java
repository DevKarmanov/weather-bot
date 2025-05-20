package org.example.service.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.karmanov.library.service.notify.Notifier;
import org.example.dto.ForecastResponse;
import org.example.dto.WeatherDto;
import org.example.dto.WeatherResponse;
import org.example.service.utils.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

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

    public String formatWeatherInformation(WeatherResponse weather, ForecastResponse forecast){
        double currentTemp = weather.getCurrent().getTemp_c();
        String currentCondition = weather.getCurrent().getCondition().getText();

        ForecastResponse.Hour hourAfter2 = forecast.forecast.forecastday.get(0).hour.get(2);
        double tempAfter2 = hourAfter2.temp_c;
        String conditionAfter2 = hourAfter2.condition.text;

        String city = String.format("🌍 Город: %s (%s)%n", weather.getLocation().getName(), weather.getLocation().getCountry());
        String temperature = String.format("🌡 Температура сейчас: %.1f°C%n", currentTemp);
        String state = String.format("☁️ Состояние сейчас: %s%n", currentCondition);
        String forecastText = String.format("⏳ Через 2 часа: %.1f°C, %s%n", tempAfter2, conditionAfter2);

        return city + temperature + state + forecastText;
    }

    public WeatherDto getWeather(String city){
        Map<String,String> queryParams =  Map.of(
                "key",WEATHER_API_KEY,
                "q",city,
                "aqi","no",
        "lang","ru");

        return getWeather(queryParams);
    }

    private WeatherDto getWeather(Map<String,String> queryParams){
        CompletableFuture<String> currentWeatherFuture = CompletableFuture.supplyAsync(() ->
                apiService.getRequest("https", "api.weatherapi.com", "/v1/current.json", queryParams, String.class));

        CompletableFuture<String> forecastWeatherFuture = CompletableFuture.supplyAsync(() ->
                apiService.getRequest("https", "api.weatherapi.com", "/v1/forecast.json", queryParams, String.class));

        return new WeatherDto(currentWeatherFuture.join(), forecastWeatherFuture.join());
    }

    public SendMessage getWeatherWithForecast(String text, Long chatId, boolean askAI,boolean useCache,boolean refreshCache) throws JsonProcessingException {
        notifier.sendMessage(chatId, "Узнаю прогноз погоды...");

        WeatherDto weatherDto = getWeather(text);

        return getForecast(weatherDto,chatId,askAI,useCache,refreshCache);

    }


    private SendMessage getForecast(WeatherDto weatherDto,Long chatId,boolean askAI,boolean useCache,boolean refreshCache) throws JsonProcessingException {
        String responseAboutCurrentWeather = weatherDto.responseAboutCurrentWeather();
        String responseAboutFutureWeather = weatherDto.responseAboutFutureWeather();

        WeatherResponse weather = mapper.readValue(responseAboutCurrentWeather, WeatherResponse.class);
        ForecastResponse forecast = mapper.readValue(responseAboutFutureWeather, ForecastResponse.class);

        String fullMessage = formatWeatherInformation(weather,forecast);

        if (!askAI) {
            return new SendMessage(String.valueOf(chatId), fullMessage);
        }


        notifier.sendMessage(chatId, fullMessage);
        notifier.sendMessage(chatId, "Попросим рекомендации у ИИ...");

        try {
            return new SendMessage(String.valueOf(chatId),
                    Converter.convert(aiService.getAdvice(responseAboutCurrentWeather,responseAboutFutureWeather,useCache,refreshCache).getChoices().get(0).getMessage().getContent()));
        }catch (Exception e){
            log.error("Ошибка при обращении к ИИ", e);
            return new SendMessage(String.valueOf(chatId),"Ошибка обращения к ИИ");
        }
    }
}
