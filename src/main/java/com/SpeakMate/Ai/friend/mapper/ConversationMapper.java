package com.SpeakMate.Ai.friend.mapper;

import com.SpeakMate.Ai.friend.dto.ConversationDto;
import com.SpeakMate.Ai.friend.entities.Conversation;

public class ConversationMapper {

    public static ConversationDto toDto(Conversation conversation) {

        return new ConversationDto(
                conversation.getId(),
                conversation.getAiQuestion(),
                conversation.getUserAnswer(),
                conversation.getAiFeedback(),
                conversation.getScore(),
                conversation.getSession().getId()
        );
    }

    public static Conversation toEntity(ConversationDto dto) {

        Conversation conversation = new Conversation();

        conversation.setId(dto.getId());
        conversation.setAiQuestion(dto.getAiQuestion());
        conversation.setUserAnswer(dto.getUserAnswer());
        conversation.setAiFeedback(dto.getAiFeedback());
        conversation.setScore(dto.getScore());

        return conversation;
    }
}