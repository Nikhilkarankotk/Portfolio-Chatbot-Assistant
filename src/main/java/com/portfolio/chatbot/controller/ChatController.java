package com.portfolio.chatbot.controller;

import com.portfolio.chatbot.dto.ChatRequest;
import com.portfolio.chatbot.model.ChatMessage;
import com.portfolio.chatbot.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping(value = "/chat", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChatMessage> handleMessage(
            @RequestBody ChatRequest request,
            @RequestHeader(value = "X-Session-ID", required = false, defaultValue = "default-session") String sessionId) {
        return ResponseEntity.ok(
            chatService.processUserMessage(sessionId, request.getMessage())
        );
    }
    @PostMapping(value = "/chat", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ChatMessage> handleMessage(
            @RequestPart("message") String message,
            @RequestPart("pdf") MultipartFile pdfFile,
            @RequestHeader(value = "X-Session-ID", required = false, defaultValue = "default-session") String sessionId) {

        // Process PDF file if needed
        // For example: extract text, metadata, etc.

        return ResponseEntity.ok(
                chatService.processUserMessage(sessionId, message, pdfFile)
        );
    }
    @GetMapping("/history")
    public ResponseEntity<List<ChatMessage>> getHistory(
            @RequestHeader(value = "X-Session-ID", required = false, defaultValue = "default-session") String sessionId) {
        return ResponseEntity.ok(
            chatService.getChatHistory(sessionId)
        );
    }

    @GetMapping("/history/sessions")
    public ResponseEntity<List<ChatMessage>> getSessions() {
        return ResponseEntity.ok(
            chatService.getRecentChatSessions()
        );
    }

    @DeleteMapping("/history/sessions/{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable String sessionId) {
        chatService.deleteChatSession(sessionId);
        return ResponseEntity.ok().build();
    }
}
