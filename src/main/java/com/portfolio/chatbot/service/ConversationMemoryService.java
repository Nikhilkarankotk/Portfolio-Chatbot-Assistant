package com.portfolio.chatbot.service;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class ConversationMemoryService {

    private static final Logger log = LoggerFactory.getLogger(ConversationMemoryService.class);
    private static final int MAX_HISTORY = 10;

    // Local in-memory cache
    private final Map<String, List<Object>> localCache = new ConcurrentHashMap<>();

    public ConversationMemoryService() {
    }

    public void addExchange(String sessionId, String userMessage, String aiResponse) {
        Map<String, String> exchange = Map.of(
            "user", userMessage,
            "assistant", aiResponse,
            "timestamp", String.valueOf(System.currentTimeMillis())
        );

        List<Object> history = localCache.computeIfAbsent(sessionId, k -> new ArrayList<>());
        history.add(exchange);
        if (history.size() > MAX_HISTORY) {
            history.remove(0);
        }
    }

    public List<Object> getContext(String sessionId) {
        return localCache.getOrDefault(sessionId, Collections.emptyList());
    }
}
