package org.example.service.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.AI.Message;
import org.example.dto.AI.request.ChatRequest;
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

    private final Map<String,CachedAdvice> adviceMap = new HashMap<>();
    private final ApiService apiService;
    private final ObjectMapper mapper;

    public AIService(ApiService apiService, ObjectMapper mapper) {
        this.apiService = apiService;
        this.mapper = mapper;
    }

    public OpenRouterResponse getAdvice(String responseAboutCurrentWeather, String responseAboutFutureWeather, boolean useCache, boolean refreshCache) {
        String city = extractCity(responseAboutCurrentWeather);
        long currentMillis = System.currentTimeMillis();

        log.info("Получение совета для города: {}", city);
        log.debug("useCache={}, refreshCache={}", useCache, refreshCache);

        if (useCache) {
            CachedAdvice cached = adviceMap.get(city);
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

        log.info("Формирую запрос к AI-модели для города: {}", city);
        Message messageToAI = new Message();
        messageToAI.setRole("user");
        messageToAI.setContent("""
        Ты — персональный ассистент по погоде и стилю.
        ...
        Данные о текущей погоде (JSON):
        """ + responseAboutCurrentWeather + """

        Данные о прогнозе на день (JSON):
        """ + responseAboutFutureWeather + """

        Сформулируй понятный и полезный совет. Ответ нужно выдавать в markdown формате.
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
        log.info("Ответ от OpenRouter получен");

        if (useCache || refreshCache) {
            log.debug("Сохраняю ответ в кэш для города: {}", city);
            adviceMap.put(city, new CachedAdvice(response, currentMillis));
        }

        return response;
    }



    private String extractCity(String json){
        try {
            return mapper.readTree(json).get("location").get("name").asText();
        }catch (JsonProcessingException e){
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
