package com.portfolio.chatbot.service.RAG;

import com.portfolio.chatbot.model.DocumentChunk;
import com.portfolio.chatbot.repository.DocumentChunkRepository;
import com.portfolio.chatbot.service.MistralApiService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.tika.exception.TikaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagService {
    private final DocumentParser documentParser;
    private final TextChunker textChunker;
    private final EmbeddingService embeddingService;
    private final DocumentChunkRepository chunkRepo;
    private final MistralApiService mistralApiService;

    public RagService(DocumentParser documentParser, TextChunker textChunker, EmbeddingService embeddingService, DocumentChunkRepository chunkRepo, MistralApiService mistralApiService) {
        this.documentParser = documentParser;
        this.textChunker = textChunker;
        this.embeddingService = embeddingService;
        this.chunkRepo = chunkRepo;
        this.mistralApiService = mistralApiService;
    }


    @Transactional
    public void ingestDocument(MultipartFile file, String sessionId) throws IOException, TikaException {
        chunkRepo.deleteBySessionId(sessionId); // clear existing chunks for THIS session
        String text = documentParser.parseDocument(file);
        List<String> chunks = textChunker.chunkText(text, 500);
        for (String chunk : chunks) {
            float[] embedding = embeddingService.generateEmbedding(chunk).block();
            if(embedding == null || embedding.length == 0){
                throw new IllegalStateException("Failed to generate embedding for chunk: " + chunk);
            }
            DocumentChunk docChunk = new DocumentChunk();
            docChunk.setText(chunk);
            docChunk.setSessionId(sessionId);
//            docChunk.setEmbedding(embedding);
            List<Float> embeddingList = new ArrayList<>();
            for (float f : embedding) {
                embeddingList.add(f);
            }
            docChunk.setEmbedding(embeddingList);
            System.out.println("Chunk: " + chunk);
            System.out.println("Embedding: " + Arrays.toString(embedding));
            chunkRepo.save(docChunk);
        }
    }
    public boolean isKnowledgeBaseEmpty(String sessionId) {
        return chunkRepo.countBySessionId(sessionId) == 0;
    }

    public String retrieveContext(String userQuery, String sessionId) {
        if (isKnowledgeBaseEmpty(sessionId)) {
            return "";
        }
        float[] queryEmbedding = embeddingService.generateEmbedding(userQuery).block();
        if (queryEmbedding == null) return "";
        
        List<DocumentChunk> relevantChunks = chunkRepo.findSimilarChunks(Arrays.toString(queryEmbedding), sessionId);
        return relevantChunks.stream()
                .map(DocumentChunk::getText)
                .collect(Collectors.joining("\n"));
    }

    public String query(String userQuery, String sessionId) {
        String context = retrieveContext(userQuery, sessionId);
        String augmentedPrompt = """
            You are a portfolio assistant. Use the following context to answer the user's question.
            If you don't know, say you don't know.
            Context: %s
            Question: %s
            """.formatted(context, userQuery);
        return mistralApiService.generateResponse(augmentedPrompt);
    }
}
