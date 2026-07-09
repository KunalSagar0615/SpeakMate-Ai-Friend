package com.SpeakMate.Ai.friend.controller;

import com.SpeakMate.Ai.friend.dto.AnswerRequestDto;
import com.SpeakMate.Ai.friend.dto.AnswerResponseDto;
import com.SpeakMate.Ai.friend.dto.ConversationDto;
import com.SpeakMate.Ai.friend.service.ConversationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/conversation")
public class ConversationController {

    @Autowired
    private ConversationService conversationService;

    @PostMapping("/create-conversation")
    public ResponseEntity<ConversationDto> createConversation(
            @Valid @RequestBody ConversationDto conversationDto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(conversationService.createConversation(conversationDto));
    }

    @GetMapping("/get-conversation-by-id/{id}")
    public ResponseEntity<ConversationDto> getConversationById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                conversationService.getConversationById(id)
        );
    }

    @PutMapping("/update-conversation/{id}")
    public ResponseEntity<ConversationDto> updateConversation(
            @PathVariable Long id,
            @Valid @RequestBody ConversationDto conversationDto) {

        return ResponseEntity.ok(
                conversationService.updateConversation(id, conversationDto)
        );
    }

    @DeleteMapping("/delete-conversation/{id}")
    public ResponseEntity<String> deleteConversationById(
            @PathVariable Long id) {

        conversationService.deleteConversationById(id);

        return ResponseEntity.ok("Conversation deleted successfully");
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<ConversationDto>> getConversationsBySessionId(
            @PathVariable Long sessionId) {

        return ResponseEntity.ok(
                conversationService.getConversationsBySessionId(sessionId)
        );
    }

    @GetMapping("/get-all-conversations")
    public ResponseEntity<List<ConversationDto>> getAllConversations() {

        return ResponseEntity.ok(
                conversationService.getAllConversations()
        );
    }

    @PostMapping("/start-conversation/{sessionId}")
    public ResponseEntity<ConversationDto> startConversation(
            @PathVariable Long sessionId) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(conversationService.startConversation(sessionId));
    }

    @PostMapping("/answer")
    public ResponseEntity<AnswerResponseDto> submitAnswer(
            @Valid @RequestBody AnswerRequestDto requestDto) {

        return ResponseEntity.ok(
                conversationService.submitAnswer(requestDto)
        );
    }
}