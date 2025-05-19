package org.example.service.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.AI.Message;
import org.example.dto.AI.request.ChatRequest;
import org.example.dto.AI.request.Location;
import org.example.dto.AI.response.OpenRouterResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AIService {

    private static final Logger log = LoggerFactory.getLogger(AIService.class);

    @Value("${api.ai-api-key}")
    private String CHAT_API_KEY;
    private final static long CACHE_TTL = 30 * 60 * 1000;

    private final Map<Location,CachedAdvice> adviceMap = new HashMap<>();
    private final ApiService apiService;
    private final ObjectMapper mapper;

    public AIService(ApiService apiService, ObjectMapper mapper) {
        this.apiService = apiService;
        this.mapper = mapper;
    }

    public OpenRouterResponse getAdvice(String responseAboutCurrentWeather, String responseAboutFutureWeather, boolean useCache, boolean refreshCache) {
        Location location = extractLocation(responseAboutCurrentWeather);
        long currentMillis = System.currentTimeMillis();

        log.info("Получение совета для города: {}", location);
        log.debug("useCache={}, refreshCache={}", useCache, refreshCache);

        if (useCache) {
            CachedAdvice cached = adviceMap.get(location);
            if (cached != null) {
                long age = currentMillis - cached.timestamp;
                log.debug("Найден кэш. Возраст: {} мс", age);
                if (age < CACHE_TTL) {
                    log.info("Возвращаю совет из кэша");
                    return cached.response;
                } else {
                    log.debug("Кэш устарел");
                }
            } else {
                log.debug("Совет в кэше не найден");
            }
        }

        log.info("Формирую запрос к AI-модели для города: {}", location);
        Message messageToAI = new Message();
        messageToAI.setRole("user");
        messageToAI.setContent("""
            Ты — персональный ассистент по погоде и стилю, встроенный в телеграм-бота.
            
            Твоя задача — на основе данных о текущей погоде и прогноза на ближайшие 2 часа дать краткий, понятный и полезный совет по выбору одежды и образу жизни.
            
            Обрати внимание:
            - Не упоминай точное время сейчас.
            - Учитывай климат и культурные особенности страны, для которой запрошен прогноз.
            - Не рекомендуй неподходящую одежду, например, не советуй надевать шубу, если на улице дождь и ветер.
            - Советы должны быть реалистичными и соответствовать тому, как обычно одеваются люди в этой стране при таких погодных условиях.
            - Не возвращай ответ в виде JSON или других структур данных. Ответ должен быть простым, живым, человеческим текстом.
            - Пиши так, как будто ты реально советуешь человеку в чате, а не выдаёшь структурированный результат.
            
            Данные о стране:\s""" + location.getCountry() + """
            
            Данные о текущей погоде (JSON):
            """ + responseAboutCurrentWeather + """
            
            Данные о прогнозе погоды на ближайшие 2 часа (JSON):
            """ + responseAboutFutureWeather + """
            
            Сформулируй краткий и практичный совет, как лучше одеться на ближайшие 2 часа.
            """);




        ChatRequest request = new ChatRequest(
                "deepseek/deepseek-prover-v2:free",
                List.of(messageToAI)
        );

        log.debug("Отправка запроса в OpenRouter...");
        OpenRouterResponse response = apiService.postRequest(
                "https", "openrouter.ai", "/api/v1/chat/completions",
                CHAT_API_KEY,
                "application/json",
                request,
                OpenRouterResponse.class
        );
        log.info("Ответ от OpenRouter получен: {}",response);

        if (useCache || refreshCache) {
            log.debug("Сохраняю ответ в кэш для города: {}", location);
            adviceMap.put(location, new CachedAdvice(response, currentMillis));
        }

        return response;
    }

    private Location extractLocation(String json) {
        try {
            var node = mapper.readTree(json).get("location");
            String city = node.get("name").asText();
            String country = node.get("country").asText();
            return new Location(city, country);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private static class CachedAdvice {
        OpenRouterResponse response;
        long timestamp;

        CachedAdvice(OpenRouterResponse response, long timestamp) {
            this.response = response;
            this.timestamp = timestamp;
        }
    }
}
