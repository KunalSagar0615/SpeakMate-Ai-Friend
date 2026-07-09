package com.SpeakMate.Ai.friend.serviceImpl;

import com.SpeakMate.Ai.friend.dto.AnswerRequestDto;
import com.SpeakMate.Ai.friend.dto.AnswerResponseDto;
import com.SpeakMate.Ai.friend.dto.ConversationDto;
import com.SpeakMate.Ai.friend.entities.Conversation;
import com.SpeakMate.Ai.friend.entities.Session;
import com.SpeakMate.Ai.friend.exception.ResourceNotFoundException;
import com.SpeakMate.Ai.friend.mapper.ConversationMapper;
import com.SpeakMate.Ai.friend.repository.ConversationRepository;
import com.SpeakMate.Ai.friend.repository.SessionRepository;
import com.SpeakMate.Ai.friend.service.AiService;
import com.SpeakMate.Ai.friend.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConversationServiceImpl implements ConversationService {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private AiService aiService;


    @Override
    public ConversationDto createConversation(ConversationDto conversationDto) {

        Session session = sessionRepository.findById(conversationDto.getSessionId()).orElseThrow(() ->new ResourceNotFoundException("Session not found with id : "+ conversationDto.getSessionId()));

        Conversation conversation = ConversationMapper.toEntity(conversationDto);
        conversation.setSession(session);

        String feedback = aiService.generateFeedback(conversationDto.getAiQuestion(), conversationDto.getUserAnswer(),session.getMode());
        conversation.setAiFeedback(feedback);

        Conversation saved = conversationRepository.save(conversation);
        return ConversationMapper.toDto(saved);
    }

    @Override
    public ConversationDto getConversationById(Long id) {

        Conversation conversation = conversationRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Conversation not found with id : " + id));
        return ConversationMapper.toDto(conversation);
    }

    @Override
    public List<ConversationDto> getAllConversations() {
        return conversationRepository.findAll().stream().map(ConversationMapper::toDto).toList();
    }

    @Override
    public ConversationDto updateConversation(Long id, ConversationDto conversationDto) {

        Conversation existingConversation = conversationRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Conversation not found with id : " + id));

        Session session = sessionRepository.findById(conversationDto.getSessionId()).orElseThrow(() -> new ResourceNotFoundException("Session not found with id : " + conversationDto.getSessionId()));

        existingConversation.setAiQuestion(conversationDto.getAiQuestion());
        existingConversation.setUserAnswer(conversationDto.getUserAnswer());
        existingConversation.setAiFeedback(conversationDto.getAiFeedback());
        existingConversation.setScore(conversationDto.getScore());
        existingConversation.setSession(session);

        Conversation updated = conversationRepository.save(existingConversation);

        return ConversationMapper.toDto(updated);
    }

    @Override
    public void deleteConversationById(Long id) {
        Conversation conversation = conversationRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Conversation not found with id : " + id));
        conversationRepository.delete(conversation);
    }

    @Override
    public List<ConversationDto> getConversationsBySessionId(Long sessionId) {
        return conversationRepository.findBySessionId(sessionId).stream().map(ConversationMapper::toDto).toList();
    }

    @Override
    public ConversationDto startConversation(Long sessionId) {

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Session not found with id : " + sessionId));

        String firstQuestion = aiService.generateQuestion(session.getTopic(),session.getMode(),session.getDifficultyLevel());

        Conversation conversation = new Conversation();

        conversation.setSession(session);
        conversation.setAiQuestion(firstQuestion);
        conversation.setUserAnswer(null);
        conversation.setAiFeedback(null);
        conversation.setScore(null);

        Conversation savedConversation =
                conversationRepository.save(conversation);

        return ConversationMapper.toDto(savedConversation);
    }

    @Override
    public AnswerResponseDto submitAnswer(
            AnswerRequestDto requestDto) {

        Conversation currentConversation =
                conversationRepository.findById(
                                requestDto.getConversationId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Conversation not found with id : "
                                                + requestDto.getConversationId()));

        currentConversation.setUserAnswer(
                requestDto.getAnswer());

        String feedback =
                aiService.generateFeedback(
                        currentConversation.getAiQuestion(),
                        requestDto.getAnswer(),currentConversation.getSession().getMode());

        currentConversation.setAiFeedback(feedback);

        conversationRepository.save(currentConversation);

        Session session =
                currentConversation.getSession();

        String nextQuestion =
                aiService.generateNextQuestion(
                        session.getTopic(),
                        currentConversation.getAiQuestion(),
                        requestDto.getAnswer(), session.getMode(),session.getDifficultyLevel());

        Conversation nextConversation =
                new Conversation();

        nextConversation.setSession(session);
        nextConversation.setAiQuestion(nextQuestion);

        Conversation savedNextConversation =
                conversationRepository.save(nextConversation);

        return new AnswerResponseDto(
                feedback,
                savedNextConversation.getId(),
                nextQuestion
        );
    }
}