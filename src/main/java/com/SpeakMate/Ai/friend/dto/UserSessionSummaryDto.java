package com.SpeakMate.Ai.friend.dto;

import com.SpeakMate.Ai.friend.enumeration.DifficultyLevel;
import com.SpeakMate.Ai.friend.enumeration.SessionMode;
import com.SpeakMate.Ai.friend.enumeration.SessionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSessionSummaryDto {

    private Long sessionId;

    private String topic;

    private SessionMode mode;

    private DifficultyLevel difficulty;

    private SessionStatus status;

    private LocalDateTime startedAt;
}
