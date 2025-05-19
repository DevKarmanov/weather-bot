package org.example.service.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.AI.Message;
import org.example.dto.AI.request.ChatRequest;
import org.example.dto.AI.response.OpenRouterResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AIService {

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

        if (useCache) {
            CachedAdvice cached = adviceMap.get(city);
            if (cached != null && currentMillis - cached.timestamp < CACHE_TTL) {
                return cached.response;
            }
        }

        Message messageToAI = new Message();
        messageToAI.setRole("user");
        messageToAI.setContent("""
    Ты — персональный ассистент по погоде и стилю.
    Твоя задача — на основе погодных данных:
    1. Кратко опиши, какая будет погода в ближайшие два часа;
    2. Дай реалистичную рекомендацию, как одеться для выхода на улицу.

    Учитывай следующее:
    - Местоположение пользователя (страна и город);
    - Текущую температуру и погодные условия;
    - Прогноз на ближайшие часы;
    - Культурные и климатические особенности региона;
    - Практичность и комфорт одежды.

    Формат ответа:
    Сначала — 1-2 предложения с описанием погоды в ближайшие два часа.
    Затем — 1-2 предложения с советом по одежде.

    Не используй технические термины и не пиши JSON. Просто объясни понятным языком.

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

        OpenRouterResponse response = apiService.postRequest(
                "https", "openrouter.ai", "/api/v1/chat/completions",
                CHAT_API_KEY,
                "application/json",
                request,
                OpenRouterResponse.class
        );

        if (useCache || refreshCache) {
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
