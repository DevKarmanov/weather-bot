package org.example.service.api;

import jakarta.annotation.PostConstruct;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class ApiService {
    private WebClient webClient;

    @PostConstruct
    public void init(){
        webClient = WebClient.create();
    }

    public <T> T getRequest(String scheme, String host, String path, Map<String, String> queryParams, Class<T> responseType) {
        return webClient.get()
                .uri(uriBuilder -> {
                    UriBuilder builder = uriBuilder
                            .scheme(scheme)
                            .host(host)
                            .path(path);
                    queryParams.forEach(builder::queryParam);
                    return builder.build();
                })
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        Mono.error(new RuntimeException("Ошибка запроса: " + clientResponse.statusCode()))
                )
                .bodyToMono(responseType)
                .block();
    }

    public <T,V> T postRequest(String scheme, String host, String path, String token, String contentType, V body, Class<T> responseType) {
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .scheme(scheme)
                        .host(host)
                        .path(path).build())
                .headers(httpHeaders -> {
                    httpHeaders.setBearerAuth(token);
                    httpHeaders.add("Content-Type", contentType);
                })
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        Mono.error(new RuntimeException("Ошибка запроса: " + clientResponse.statusCode()))
                )
                .bodyToMono(responseType)
                .block();
    }
}
