package com.SpeakMate.Ai.friend.controller;

import com.SpeakMate.Ai.friend.dto.TongueTwisterHistoryDto;
import com.SpeakMate.Ai.friend.dto.TongueTwisterResponseDto;
import com.SpeakMate.Ai.friend.dto.TongueTwisterStatsDto;
import com.SpeakMate.Ai.friend.service.TongueTwisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/tongue-twister")
public class TongueTwisterController {

    private final TongueTwisterService tongueTwisterService;

    @Autowired
    public TongueTwisterController(
            TongueTwisterService tongueTwisterService
    ) {
        this.tongueTwisterService =
                tongueTwisterService;
    }

    // =========================================================
    // START SESSION
    // =========================================================

    @PostMapping("/start")
    public ResponseEntity<TongueTwisterResponseDto> startSession() {

        TongueTwisterResponseDto response =
                tongueTwisterService.startSession();

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // GENERATE NEW PASSAGE
    // =========================================================

    @PostMapping("/generate/{sessionId}")
    public ResponseEntity<TongueTwisterResponseDto> generateNewPassage(
            @PathVariable Long sessionId
    ) {

        TongueTwisterResponseDto response =
                tongueTwisterService.generateNewPassage(
                        sessionId
                );

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // END SESSION
    // =========================================================

    @PostMapping("/end/{sessionId}")
    public ResponseEntity<TongueTwisterResponseDto> endSession(
            @PathVariable Long sessionId
    ) {

        TongueTwisterResponseDto response =
                tongueTwisterService.endSession(
                        sessionId
                );

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // STATISTICS
    // =========================================================

    @GetMapping("/stats")
    public ResponseEntity<TongueTwisterStatsDto> getStats() {

        TongueTwisterStatsDto stats =
                tongueTwisterService.getStats();

        return ResponseEntity.ok(stats);
    }

    // =========================================================
    // HISTORY
    // =========================================================

    @GetMapping("/history")
    public ResponseEntity<List<TongueTwisterHistoryDto>> getHistory() {

        List<TongueTwisterHistoryDto> history =
                tongueTwisterService.getHistory();

        return ResponseEntity.ok(history);
    }

    // =========================================================
    // DOWNLOAD HISTORY
    // =========================================================

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadHistory() {

        String content =
                tongueTwisterService.downloadHistory();

        byte[] file =
                content.getBytes(
                        StandardCharsets.UTF_8
                );

        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                MediaType.TEXT_PLAIN
        );

        headers.setContentDisposition(
                ContentDisposition
                        .attachment()
                        .filename(
                                "tongue-twister-history.txt",
                                StandardCharsets.UTF_8
                        )
                        .build()
        );

        headers.setContentLength(
                file.length
        );

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(file);
    }
}