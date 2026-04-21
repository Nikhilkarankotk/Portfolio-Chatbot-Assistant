package com.portfolio.chatbot.model;

import com.portfolio.chatbot.service.RAG.converter.FloatArrayConverter;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class DocumentChunk {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(columnDefinition = "TEXT")
    private String text;
//    @Convert(converter = FloatArrayConverter.class)
//    @Column(columnDefinition = "vector(1536)")
//    private float[] embedding; // Store embeddings as float[]
    @ElementCollection
    @CollectionTable(name = "document_chunk_embeddings", joinColumns = @JoinColumn(name = "chunk_id"))
    @Column(name = "embedding_value")
    private List<Float> embedding;

    public List<Float> getEmbedding() {
        return embedding;
    }

    public void setEmbedding(List<Float> embedding) {
        this.embedding = embedding;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
