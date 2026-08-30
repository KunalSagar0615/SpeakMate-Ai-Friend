package com.SpeakMate.Ai.friend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TongueTwisterStatsDto {

    private Long totalSessions;

    private Long completedSessions;

    private Long cancelledSessions;

    private Long totalPassages;
}