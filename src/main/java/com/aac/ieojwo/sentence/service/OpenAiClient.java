package com.aac.ieojwo.sentence.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * OpenAI Chat Completions 호출. API 키는 서버 환경변수에만 둔다(앱에 넣지 않음).
 * 키가 없거나, 시간 초과·오류가 나면 예외 대신 Optional.empty() 를 돌려준다 — 호출 쪽이 카드 라벨로 대체한다.
 * 타임아웃은 spring.http.client.connect-timeout / read-timeout 으로 설정한다(application.yml).
 */
@Component
public class OpenAiClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiClient.class);
    private static final String BASE_URL = "https://api.openai.com/v1";

    private final RestClient restClient;
    private final String model;
    private final boolean enabled;

    public OpenAiClient(RestClient.Builder builder,
                        @Value("${app.openai.api-key:}") String apiKey,
                        @Value("${app.openai.model:gpt-4.1-mini}") String model) {
        this.enabled = apiKey != null && !apiKey.isBlank();
        this.model = model;
        this.restClient = builder.baseUrl(BASE_URL)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
        if (!enabled) {
            log.info("OpenAI API 키가 설정되지 않아 문장 생성은 카드 라벨 이어붙이기로 대체됩니다. (app.openai.api-key)");
        }
    }

    public Optional<String> complete(String systemPrompt, String userPrompt) {
        if (!enabled) {
            return Optional.empty();
        }
        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                )
        );
        try {
            JsonNode response = restClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
            String text = response == null ? ""
                    : response.path("choices").path(0).path("message").path("content").asText("").strip();
            return text.isEmpty() ? Optional.empty() : Optional.of(text);
        } catch (RestClientException e) {
            log.warn("OpenAI 문장 생성 실패: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
