package com.SpeakMate.Ai.friend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TongueTwisterHistoryDto {

    private Long passageId;

    private String passage;

    private Integer wordCount;

    private LocalDateTime createdAt;
}