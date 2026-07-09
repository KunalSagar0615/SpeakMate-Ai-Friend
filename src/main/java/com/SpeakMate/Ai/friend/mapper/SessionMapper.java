package com.SpeakMate.Ai.friend.mapper;

import com.SpeakMate.Ai.friend.dto.SessionDto;
import com.SpeakMate.Ai.friend.entities.Session;

public class SessionMapper {

    public static SessionDto toDto(Session session) {

        return new SessionDto(
                session.getId(),
                session.getTopic(),
                session.getStartTime(),
                session.getEndTime(),
                session.getStatus(),
                session.getUser() != null ? session.getUser().getId() : null,
                session.getMode(),
                session.getDifficultyLevel()
        );
    }

    public static Session toEntity(SessionDto dto) {

        Session session = new Session();

        session.setId(dto.getId());
        session.setTopic(dto.getTopic());
        session.setStartTime(dto.getStartTime());
        session.setEndTime(dto.getEndTime());
        session.setStatus(dto.getStatus());
        session.setMode(dto.getMode());
        session.setDifficultyLevel(dto.getDifficultyLevel());

        return session;
    }
}