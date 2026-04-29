package com.portfolio.chatbot.service;

import com.portfolio.chatbot.dto.TranslationResult;
import com.portfolio.chatbot.model.ChatMessage;
import com.portfolio.chatbot.repository.ChatMessageRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class ChatService {

    private final ChatMessageRepository chatRepo;
    private final MistralApiService mistralService;
    private final ConversationMemoryService memoryService;
    private final TranslationService translationService;
    private final com.portfolio.chatbot.service.RAG.RagService ragService;

    @Autowired
    public ChatService(ChatMessageRepository chatRepo, 
                       MistralApiService mistralService,
                       ConversationMemoryService memoryService,
                       TranslationService translationService,
                       com.portfolio.chatbot.service.RAG.RagService ragService) {
        this.chatRepo = chatRepo;
        this.mistralService = mistralService;
        this.memoryService = memoryService;
        this.translationService = translationService;
        this.ragService = ragService;
    }

    @Transactional
    public ChatMessage processUserMessage(String sessionId, String userMessage) {
        // Standardizing on English for now
        String userLangCode = "en";
        String userLangName = "English";
        String translatedUserQuery = userMessage;

        // 1. Save Original User Message to DB
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSessionId(sessionId);
        userMsg.setContent(userMessage);
        userMsg.setRole("user");
        chatRepo.save(userMsg);

        // 2. Retrieve Knowledge Context (RAG)
        String knowledgeContext = ragService.retrieveContext(userMessage, sessionId);

        // 3. Retrieve Conversation History (Memory)
        List<Object> history = memoryService.getContext(sessionId);
        
        // 4. Construct Augmented Prompt
        StringBuilder augmentedPrompt = new StringBuilder();

        augmentedPrompt.append("System: You are an advanced AI assistant powered by a Large Language Model architecture. ")
                       .append("You are helpful, professional, and capable of general reasoning as well as document-specific analysis.\n\n");

        if (knowledgeContext != null && !knowledgeContext.isBlank()) {
            augmentedPrompt.append("DOCUMENTS CONTEXT:\n")
                           .append("The following information has been retrieved from the user's uploaded documents. ")
                           .append("Prioritize this information for accuracy if the user's question relates to it:\n")
                           .append(knowledgeContext).append("\n\n");
        }

        augmentedPrompt.append("INSTRUCTIONS:\n")
                       .append("- Answer the user's request accurately and conversationally.\n")
                       .append("- If DOCUMENTS CONTEXT is provided above, use it to ground your response.\n")
                       .append("- If no context is provided, or the context doesn't contain the answer, rely on your extensive general knowledge to assist the user.\n")
                       .append("- Do not explicitly mention the presence or absence of documents unless it is directly relevant to the user's query.\n\n");
        
        for (Object exchangeObj : history) {
            Map<String, String> exchange = (Map<String, String>) exchangeObj;
            augmentedPrompt.append("User: ").append(exchange.get("user")).append("\n");
            augmentedPrompt.append("Assistant: ").append(exchange.get("assistant")).append("\n");
        }
        augmentedPrompt.append("User: ").append(userMessage);

        // 5. Get AI Response
        String finalResponse = mistralService.generateResponse(augmentedPrompt.toString());

        // 6. Store Exchange in Redis
        memoryService.addExchange(sessionId, userMessage, finalResponse);

        // 7. Save Assistant Response to DB
        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setSessionId(sessionId);
        aiMsg.setContent(finalResponse);
        aiMsg.setRole("assistant");
        aiMsg.setDetectedLanguage(userLangCode);
        aiMsg.setIsTranslated(false);
        aiMsg.setTimestamp(LocalDateTime.now());
        return chatRepo.save(aiMsg);
    }

    @Transactional
    public ChatMessage processUserMessage(String sessionId, String userMessage, MultipartFile file) {
        try {
            ragService.ingestDocument(file, sessionId);
        } catch (Exception e) {
            // Log error but proceed with chat? Or return error message?
            System.err.println("Error ingesting document in chat: " + e.getMessage());
        }
        return processUserMessage(sessionId, userMessage);
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
