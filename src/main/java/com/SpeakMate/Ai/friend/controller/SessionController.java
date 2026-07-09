package com.SpeakMate.Ai.friend.controller;

import com.SpeakMate.Ai.friend.dto.SessionDto;
import com.SpeakMate.Ai.friend.dto.SessionReportDto;
import com.SpeakMate.Ai.friend.dto.SessionSummaryDto;
import com.SpeakMate.Ai.friend.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/session")
public class SessionController {

    @Autowired
    private SessionService sessionService;

    @PostMapping("/create-session")
    public ResponseEntity<SessionDto> createSession(
            @Valid @RequestBody SessionDto sessionDto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sessionService.createSession(sessionDto));
    }

    @GetMapping("/get-session-by-id/{id}")
    public ResponseEntity<SessionDto> getSessionById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                sessionService.getSessionById(id)
        );
    }

    @GetMapping("/get-all-sessions")
    public ResponseEntity<List<SessionDto>> getAllSessions() {

        return ResponseEntity.ok(
                sessionService.getAllSessions()
        );
    }

    @PutMapping("/update-session-by-id/{id}")
    public ResponseEntity<SessionDto> updateSessionById(
            @PathVariable Long id,
            @Valid @RequestBody SessionDto sessionDto) {

        return ResponseEntity.ok(
                sessionService.updateSession(id, sessionDto)
        );
    }

    @DeleteMapping("/delete-session-by-id/{id}")
    public ResponseEntity<String> deleteSessionById(
            @PathVariable Long id) {

        sessionService.deleteSessionById(id);

        return ResponseEntity.ok("Session deleted successfully");
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SessionDto>> getSessionsByUserId(
            @PathVariable Long userId) {

        List<SessionDto> sessions =
                sessionService.getSessionsByUserId(userId);

        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/summary/{sessionId}")
    public ResponseEntity<SessionSummaryDto> getSessionSummary(
            @PathVariable Long sessionId) {

        return ResponseEntity.ok(
                sessionService.getSessionSummary(sessionId)
        );
    }

    @PutMapping("/end/{sessionId}")
    public ResponseEntity<SessionDto> endSession(
            @PathVariable Long sessionId) {

        return ResponseEntity.ok(
                sessionService.endSession(sessionId)
        );
    }

    @GetMapping("/report/{sessionId}")
    public ResponseEntity<SessionReportDto> getSessionReport(
            @PathVariable Long sessionId) {

        return ResponseEntity.ok(
                sessionService.getSessionReport(sessionId)
        );
    }
}