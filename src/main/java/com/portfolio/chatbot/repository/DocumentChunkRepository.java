package com.portfolio.chatbot.repository;

import com.portfolio.chatbot.model.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {
//    @Query(value = "SELECT * FROM document_chunk ORDER BY embedding <=> ?::vector LIMIT 5", nativeQuery = true)
//    List<DocumentChunk> findSimilarChunks(float[] embedding);

    @Query(value = """
    SELECT * FROM document_chunk
    WHERE session_id = :sessionId
    ORDER BY embedding <-> CAST(:embedding AS vector)
    LIMIT 5
    """, nativeQuery = true)
    List<DocumentChunk> findSimilarChunks(String embedding, String sessionId);

    long countBySessionId(String sessionId);

    void deleteBySessionId(String sessionId);
}


