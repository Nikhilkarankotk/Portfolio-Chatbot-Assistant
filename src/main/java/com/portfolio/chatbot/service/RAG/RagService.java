package com.portfolio.chatbot.service.RAG;

import com.portfolio.chatbot.model.DocumentChunk;
import com.portfolio.chatbot.repository.DocumentChunkRepository;
import com.portfolio.chatbot.service.MistralApiService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
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
    public void ingestDocument(MultipartFile file) throws IOException, TikaException {
        chunkRepo.deleteAll(); // clear existing chunks
        String text = documentParser.parseDocument(file);
        List<String> chunks = textChunker.chunkText(text, 500);
        for (String chunk : chunks) {
            float[] embedding = embeddingService.generateEmbedding(chunk).block();
            if(embedding == null || embedding.length == 0){
                throw new IllegalStateException("Failed to generate embedding for chunk: " + chunk);
            }
            DocumentChunk docChunk = new DocumentChunk();
            docChunk.setText(chunk);
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
    public String query(String userQuery) {
        float[] queryEmbedding = embeddingService.generateEmbedding(userQuery).block();
        System.out.println("Query Embedding: " + Arrays.toString(queryEmbedding));
        List<DocumentChunk> relevantChunks = chunkRepo.findSimilarChunks(Arrays.toString(queryEmbedding));
        System.out.println("Retrieved Chunks: " + relevantChunks);
        String context = relevantChunks.stream()
                .map(DocumentChunk::getText)
                .collect(Collectors.joining("\n"));
        String augmentedPrompt = """
            You are a portfolio assistant. Use the following context to answer the user's question.
            If you dont know, say you dont know.
            Context: %s
            Question: %s
            """.formatted(context, userQuery);
        return mistralApiService.generateResponse(augmentedPrompt);
    }
}
