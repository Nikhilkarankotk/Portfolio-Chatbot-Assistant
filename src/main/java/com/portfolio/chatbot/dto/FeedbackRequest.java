package com.portfolio.chatbot.dto;

import lombok.Data;

public class FeedbackRequest {
    private Long messageId;
    private Integer rating;
    private String correctedAnswer;

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getCorrectedAnswer() {
        return correctedAnswer;
    }

    public void setCorrectedAnswer(String correctedAnswer) {
        this.correctedAnswer = correctedAnswer;
    }
}
