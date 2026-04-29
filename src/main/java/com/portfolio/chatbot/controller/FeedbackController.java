package com.portfolio.chatbot.controller;

import com.portfolio.chatbot.dto.FeedbackRequest;
import com.portfolio.chatbot.model.ChatMessage;
import com.portfolio.chatbot.model.Feedback;
import com.portfolio.chatbot.repository.ChatMessageRepository;
import com.portfolio.chatbot.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackRepository feedbackRepo;
    private final ChatMessageRepository chatRepo;

    public FeedbackController(FeedbackRepository feedbackRepo, ChatMessageRepository chatRepo) {
        this.feedbackRepo = feedbackRepo;
        this.chatRepo = chatRepo;
    }

    @PostMapping
    public ResponseEntity<Void> submitFeedback(@RequestBody FeedbackRequest request) {
        ChatMessage message = chatRepo.findById(request.getMessageId())
                .orElseThrow(() -> new RuntimeException("Message not found"));

        Feedback feedback = feedbackRepo.findByMessageId(request.getMessageId())
                                        .orElseGet(Feedback::new);

        if (feedback.getId() == null) {
            feedback.setMessage(message);
        }
        
        feedback.setRating(request.getRating());
        feedback.setCorrectedAnswer(request.getCorrectedAnswer());
        
        feedbackRepo.save(feedback);
        return ResponseEntity.ok().build();
    }
}
