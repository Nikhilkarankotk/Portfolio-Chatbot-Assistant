package com.portfolio.chatbot.repository;

import com.portfolio.chatbot.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findBySessionId(String sessionId);

    @Query("SELECT c FROM ChatMessage c WHERE c.id IN (SELECT MIN(m.id) FROM ChatMessage m WHERE m.role = 'user' GROUP BY m.sessionId) ORDER BY c.timestamp DESC")
    List<ChatMessage> findRecentChatSessions();

    void deleteBySessionId(String sessionId);
}
