package com.SpeakMate.Ai.friend.service;

import com.SpeakMate.Ai.friend.dto.CustomPracticeReportDto;
import com.SpeakMate.Ai.friend.entities.CustomPracticeSession;

public interface CustomPracticeReportService {

    void generateAndStoreReport(
            CustomPracticeSession session
    );

    CustomPracticeReportDto getReport(
            Long sessionId
    );
}