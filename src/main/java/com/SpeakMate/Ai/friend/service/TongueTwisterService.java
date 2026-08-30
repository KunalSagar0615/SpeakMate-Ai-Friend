package com.SpeakMate.Ai.friend.service;

import com.SpeakMate.Ai.friend.dto.TongueTwisterHistoryDto;
import com.SpeakMate.Ai.friend.dto.TongueTwisterResponseDto;
import com.SpeakMate.Ai.friend.dto.TongueTwisterStatsDto;

import java.util.List;

public interface TongueTwisterService {

    /**
     * Starts a new Tongue Twister session and generates
     * the first passage for that session.
     */
    TongueTwisterResponseDto startSession();

    /**
     * Generates a new passage inside the currently active
     * Tongue Twister session.
     */
    TongueTwisterResponseDto generateNewPassage(Long sessionId);

    /**
     * Completes the currently active Tongue Twister session.
     */
    TongueTwisterResponseDto endSession(Long sessionId);

    /**
     * Returns statistics for the currently logged-in user.
     */
    TongueTwisterStatsDto getStats();

    /**
     * Returns the complete Tongue Twister passage history
     * for the currently logged-in user.
     */
    List<TongueTwisterHistoryDto> getHistory();

    /**
     * Returns the complete passage history as downloadable text.
     */
    String downloadHistory();
}