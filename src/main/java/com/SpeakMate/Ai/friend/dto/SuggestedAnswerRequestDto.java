package com.SpeakMate.Ai.friend.dto;

import com.SpeakMate.Ai.friend.enumeration.DifficultyLevel;
import com.SpeakMate.Ai.friend.enumeration.SessionMode;

public class SuggestedAnswerRequestDto {

    private String question;
    private SessionMode mode;
    private DifficultyLevel difficultyLevel;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public SessionMode getMode() {
        return mode;
    }

    public void setMode(SessionMode mode) {
        this.mode = mode;
    }

    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }
}
