package com.portfolio.chatbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class MistralApiService {
    private final WebClient webClient;
    public MistralApiService(@Value("${mistral.api.key}") String apiKey) {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.mistral.ai/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }
    public Mono<String> chatCompletion(String prompt) {
        return webClient.post()
                .uri("/chat/completions")
                .bodyValue(Map.of(
                    "model", "mistral-small-latest",
                    "messages", List.of(Map.of("role", "user", "content", prompt))
                ))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(java.time.Duration.ofSeconds(30))
                .retry(3)
                .map(response -> response.get("choices").get(0).get("message").get("content").asText());
    }

    public String generateResponse(String augmentedPrompt) {
        return chatCompletion(augmentedPrompt).block();
    }
}
