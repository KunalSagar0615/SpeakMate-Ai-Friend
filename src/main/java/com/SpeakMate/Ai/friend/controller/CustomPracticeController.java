package com.SpeakMate.Ai.friend.controller;

import com.SpeakMate.Ai.friend.dto.*;
import com.SpeakMate.Ai.friend.service.CustomPracticeReportService;
import com.SpeakMate.Ai.friend.service.CustomPracticeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/custom-practice")
public class CustomPracticeController {

    private final CustomPracticeService customPracticeService;
    private final CustomPracticeReportService customPracticeReportService;

    public CustomPracticeController(
            CustomPracticeService customPracticeService,
            CustomPracticeReportService customPracticeReportService) {

        this.customPracticeService =
                customPracticeService;

        this.customPracticeReportService =
                customPracticeReportService;
    }

    // =========================================================
    // QUESTION EXTRACTION
    // =========================================================

    @PostMapping("/extract-questions")
    public ResponseEntity<ExtractQuestionsResponseDto>
    extractQuestions(
            @Valid
            @RequestBody ExtractQuestionsRequestDto request) {

        return ResponseEntity.ok(
                customPracticeService
                        .extractQuestions(request)
        );
    }

    // =========================================================
    // CREATE SESSION
    // =========================================================

    @PostMapping
    public ResponseEntity<CustomPracticeSessionDto>
    createSession(
            @Valid
            @RequestBody CreateCustomPracticeRequestDto request) {

        CustomPracticeSessionDto session =
                customPracticeService
                        .createSession(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(session);
    }

    // =========================================================
    // PAUSED SESSIONS
    // =========================================================

    @GetMapping("/paused")
    public ResponseEntity<List<CustomPracticeSummaryDto>>
    getPausedSessions() {

        return ResponseEntity.ok(
                customPracticeService
                        .getPausedSessions()
        );
    }

    // =========================================================
    // SESSION HISTORY
    // =========================================================

    @GetMapping("/history")
    public ResponseEntity<List<CustomPracticeSummaryDto>>
    getSessionHistory() {

        return ResponseEntity.ok(
                customPracticeService
                        .getSessionHistory()
        );
    }

    // =========================================================
    // GET SESSION
    // =========================================================

    @GetMapping("/{sessionId}")
    public ResponseEntity<CustomPracticeSessionDto>
    getSession(
            @PathVariable Long sessionId) {

        return ResponseEntity.ok(
                customPracticeService
                        .getSession(sessionId)
        );
    }

    // =========================================================
    // SUBMIT ANSWER
    // =========================================================

    @PostMapping("/{sessionId}/answer")
    public ResponseEntity<AnswerEvaluationResponseDto>
    submitAnswer(
            @PathVariable Long sessionId,
            @Valid
            @RequestBody SubmitCustomAnswerRequestDto request) {

        return ResponseEntity.ok(
                customPracticeService
                        .submitAnswer(
                                sessionId,
                                request
                        )
        );
    }

    // =========================================================
    // SKIP CURRENT QUESTION
    // =========================================================

    @PostMapping("/{sessionId}/skip")
    public ResponseEntity<SkipQuestionResponseDto>
    skipQuestion(
            @PathVariable Long sessionId) {

        return ResponseEntity.ok(
                customPracticeService
                        .skipCurrentQuestion(
                                sessionId
                        )
        );
    }

    // =========================================================
    // NEXT QUESTION
    // =========================================================

    @PostMapping("/{sessionId}/next")
    public ResponseEntity<NextQuestionResponseDto>
    nextQuestion(
            @PathVariable Long sessionId) {

        return ResponseEntity.ok(
                customPracticeService
                        .getNextQuestion(
                                sessionId
                        )
        );
    }

    // =========================================================
    // SAVE / CLEAR DRAFT
    // =========================================================

    @PutMapping("/{sessionId}/draft")
    public ResponseEntity<Void>
    saveDraft(
            @PathVariable Long sessionId,
            @Valid
            @RequestBody SaveDraftAnswerRequestDto request) {

        customPracticeService
                .saveDraftAnswer(
                        sessionId,
                        request
                );

        return ResponseEntity
                .noContent()
                .build();
    }

    // =========================================================
    // PAUSE WITH USER-SELECTED DURATION
    // =========================================================

    @PostMapping("/{sessionId}/pause")
    public ResponseEntity<CustomPracticeSessionDto>
    pauseSession(
            @PathVariable Long sessionId,
            @Valid
            @RequestBody PauseCustomPracticeRequestDto request) {

        return ResponseEntity.ok(
                customPracticeService
                        .pauseSession(
                                sessionId,
                                request
                        )
        );
    }

    // =========================================================
    // DEFAULT 15-DAY PAUSE
    // =========================================================

    @PostMapping("/{sessionId}/pause-default")
    public ResponseEntity<CustomPracticeSessionDto>
    pauseSessionWithDefaultDuration(
            @PathVariable Long sessionId) {

        return ResponseEntity.ok(
                customPracticeService
                        .pauseSessionWithDefaultDuration(
                                sessionId
                        )
        );
    }

    // =========================================================
    // RESUME SESSION
    // =========================================================

    @PostMapping("/{sessionId}/resume")
    public ResponseEntity<CustomPracticeSessionDto>
    resumeSession(
            @PathVariable Long sessionId) {

        return ResponseEntity.ok(
                customPracticeService
                        .resumeSession(
                                sessionId
                        )
        );
    }

    // =========================================================
    // END SESSION
    // =========================================================

    @PostMapping("/{sessionId}/end")
    public ResponseEntity<EndCustomPracticeResponseDto>
    endSession(
            @PathVariable Long sessionId) {

        return ResponseEntity.ok(
                customPracticeService
                        .endSession(
                                sessionId
                        )
        );
    }

    // =========================================================
    // SESSION REPORT
    // =========================================================

    @GetMapping("/{sessionId}/report")
    public ResponseEntity<CustomPracticeReportDto>
    getReport(
            @PathVariable Long sessionId) {

        return ResponseEntity.ok(
                customPracticeReportService
                        .getReport(sessionId)
        );
    }
}