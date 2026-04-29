package com.portfolio.chatbot.controller;

import com.portfolio.chatbot.service.RAG.RagService;
import lombok.RequiredArgsConstructor;
import org.apache.tika.exception.TikaException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/ingest")
    public ResponseEntity<String> ingestDocument(
            @RequestPart("file") MultipartFile file,
            @RequestHeader(value = "X-Session-ID", required = false, defaultValue = "default-session") String sessionId) throws IOException, TikaException {
//        log.info("Ingesting document for session {}: {}", sessionId, file.getOriginalFilename());
        ragService.ingestDocument(file, sessionId);
        return ResponseEntity.ok("Document ingested successfully for session: " + sessionId);
    }

    @PostMapping("/query")
    public ResponseEntity<String> query(
            @RequestBody String userQuery,
            @RequestHeader(value = "X-Session-ID", required = false, defaultValue = "default-session") String sessionId) {
        return ResponseEntity.ok(ragService.query(userQuery, sessionId));
    }
}
