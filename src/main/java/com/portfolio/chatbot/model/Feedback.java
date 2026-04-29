package com.portfolio.chatbot.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "message_id", referencedColumnName = "id")
    private ChatMessage message;

    private Integer rating; // 1 for thumbs up, -1 for thumbs down
    
    @Column(columnDefinition = "TEXT")
    private String correctedAnswer;
    
    private LocalDateTime timestamp;

    public Feedback() {}

    public Feedback(Long id, ChatMessage message, Integer rating, String correctedAnswer, LocalDateTime timestamp) {
        this.id = id;
        this.message = message;
        this.rating = rating;
        this.correctedAnswer = correctedAnswer;
        this.timestamp = timestamp;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ChatMessage getMessage() { return message; }
    public void setMessage(ChatMessage message) { this.message = message; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getCorrectedAnswer() { return correctedAnswer; }
    public void setCorrectedAnswer(String correctedAnswer) { this.correctedAnswer = correctedAnswer; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}
