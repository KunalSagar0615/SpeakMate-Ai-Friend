package com.SpeakMate.Ai.friend.service;

import com.SpeakMate.Ai.friend.dto.SessionDto;
import com.SpeakMate.Ai.friend.dto.SessionReportDto;
import com.SpeakMate.Ai.friend.dto.SessionSummaryDto;

import java.util.List;

public interface SessionService {

    SessionDto createSession(SessionDto sessionDto);

    SessionDto getSessionById(Long id);

    List<SessionDto> getAllSessions();

    SessionDto updateSession(Long id, SessionDto sessionDto);

    void deleteSessionById(Long id);

    SessionSummaryDto getSessionSummary(Long sessionId);

    List<SessionDto> getSessionsByUserId(Long userId);

    SessionDto endSession(Long sessionId);

    SessionReportDto getSessionReport(Long sessionId);
}