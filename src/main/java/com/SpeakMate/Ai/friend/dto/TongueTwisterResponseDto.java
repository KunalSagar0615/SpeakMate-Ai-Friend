package com.SpeakMate.Ai.friend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TongueTwisterResponseDto {

    private Long sessionId;

    private Long passageId;

    private String passage;

    private Integer wordCount;

    private Long completedSessions;

    private Long totalSessions;
}