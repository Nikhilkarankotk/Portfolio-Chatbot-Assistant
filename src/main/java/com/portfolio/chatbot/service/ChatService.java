package com.portfolio.chatbot.service;

import com.portfolio.chatbot.model.ChatMessage;
import com.portfolio.chatbot.repository.ChatMessageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ChatService {

    private final ChatMessageRepository chatRepo;
    private final MistralApiService mistralService;

    @Autowired
    public ChatService(ChatMessageRepository chatRepo, MistralApiService mistralService) {
        this.chatRepo = chatRepo;
        this.mistralService = mistralService;
    }

    @Transactional
    public ChatMessage processUserMessage(String sessionId, String userMessage) {
        // Save user message
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSessionId(sessionId);
        userMsg.setContent(userMessage);
        userMsg.setRole("user");
        chatRepo.save(userMsg);
        // Get AI response
        String aiResponse = mistralService.chatCompletion(userMessage).block();
        // Save AI response
        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setSessionId(sessionId);
        aiMsg.setContent(aiResponse);
        aiMsg.setRole("assistant");
        return chatRepo.save(aiMsg);
    }
    @Transactional
    public ChatMessage processUserMessage(String sessionId, String userMessage, MultipartFile file) {
        // Save user message
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSessionId(sessionId);
        userMsg.setContent(userMessage);
        userMsg.setRole("user");
        chatRepo.save(userMsg);
        // Get AI response
        String aiResponse = mistralService.chatCompletion(userMessage).block();
        // Save AI response
        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setSessionId(sessionId);
        aiMsg.setContent(aiResponse);
        aiMsg.setRole("assistant");
        return chatRepo.save(aiMsg);
    }
    @Transactional
    public List<ChatMessage> getChatHistory(String sessionId) {
        return chatRepo.findBySessionId(sessionId);
    }

    @Transactional
    public List<ChatMessage> getRecentChatSessions() {
        return chatRepo.findRecentChatSessions();
    }

    @Transactional
    public void deleteChatSession(String sessionId) {
        chatRepo.deleteBySessionId(sessionId);
    }
}
