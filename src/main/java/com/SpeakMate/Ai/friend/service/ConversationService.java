package com.SpeakMate.Ai.friend.service;

import com.SpeakMate.Ai.friend.dto.AnswerRequestDto;
import com.SpeakMate.Ai.friend.dto.AnswerResponseDto;
import com.SpeakMate.Ai.friend.dto.ConversationDto;

import java.util.List;

public interface ConversationService {

    ConversationDto createConversation(ConversationDto conversationDto);

    ConversationDto getConversationById(Long id);

    List<ConversationDto> getAllConversations();

    ConversationDto updateConversation(Long id, ConversationDto conversationDto);

    void deleteConversationById(Long id);

    List<ConversationDto> getConversationsBySessionId(Long sessionId);

    ConversationDto startConversation(Long sessionId);

    AnswerResponseDto submitAnswer(AnswerRequestDto requestDto);
}