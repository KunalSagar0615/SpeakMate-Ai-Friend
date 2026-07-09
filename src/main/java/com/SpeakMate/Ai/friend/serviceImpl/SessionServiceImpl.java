package com.SpeakMate.Ai.friend.serviceImpl;

import com.SpeakMate.Ai.friend.dto.SessionDto;
import com.SpeakMate.Ai.friend.dto.SessionReportDto;
import com.SpeakMate.Ai.friend.dto.SessionSummaryDto;
import com.SpeakMate.Ai.friend.entities.Conversation;
import com.SpeakMate.Ai.friend.entities.Session;
import com.SpeakMate.Ai.friend.entities.User;
import com.SpeakMate.Ai.friend.enumeration.SessionStatus;
import com.SpeakMate.Ai.friend.exception.ResourceNotFoundException;
import com.SpeakMate.Ai.friend.mapper.SessionMapper;
import com.SpeakMate.Ai.friend.repository.ConversationRepository;
import com.SpeakMate.Ai.friend.repository.SessionRepository;
import com.SpeakMate.Ai.friend.repository.UserRepository;
import com.SpeakMate.Ai.friend.service.AiService;
import com.SpeakMate.Ai.friend.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SessionServiceImpl implements SessionService {

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private AiService aiService;

    @Override
    public SessionDto createSession(SessionDto sessionDto) {

        User user = userRepository.findById(sessionDto.getUserId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id : "
                                        + sessionDto.getUserId()));

        Session session = SessionMapper.toEntity(sessionDto);

        session.setUser(user);
        session.setStartTime(LocalDateTime.now());

        if (sessionDto.getStatus() == SessionStatus.COMPLETED) {
            session.setEndTime(LocalDateTime.now());
        }

        Session savedSession = sessionRepository.save(session);
        return SessionMapper.toDto(savedSession);
    }

    @Override
    public SessionDto getSessionById(Long id) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Session not found with given id : " + id));

        return SessionMapper.toDto(session);
    }

    @Override
    public List<SessionDto> getAllSessions() {
        return sessionRepository.findAll()
                .stream()
                .map(SessionMapper::toDto)
                .toList();
    }

    @Override
    public SessionDto updateSession(Long id, SessionDto sessionDto) {

        Session session = sessionRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Session not found with id : " + id));

        User user = userRepository.findById(sessionDto.getUserId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id : "
                                        + sessionDto.getUserId()));

        session.setTopic(sessionDto.getTopic());
        session.setStartTime(sessionDto.getStartTime());
        session.setEndTime(sessionDto.getEndTime());
        session.setStatus(sessionDto.getStatus());
        session.setMode(sessionDto.getMode());
        session.setUser(user);

        Session updatedSession = sessionRepository.save(session);

        return SessionMapper.toDto(updatedSession);
    }

    @Override
    public List<SessionDto> getSessionsByUserId(Long userId) {

        return sessionRepository.findByUserId(userId)
                .stream()
                .map(SessionMapper::toDto)
                .toList();
    }

    @Override
    public void deleteSessionById(Long id) {

        Session session = sessionRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Session not found with given id : " + id));

        sessionRepository.delete(session);
    }

    @Override
    public SessionSummaryDto getSessionSummary(Long sessionId) {

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Session not found with id : " + sessionId));

        List<Conversation> conversations =
                conversationRepository.findBySessionId(sessionId);

        int totalQuestions = conversations.size();

        int totalScore = conversationRepository.sumScoreBySessionId(sessionId);

        double averageScore =conversationRepository.averageScoreBySessionId(sessionId);

        return new SessionSummaryDto(
                session.getId(),
                session.getTopic(),
                totalQuestions,
                totalScore,
                averageScore,
                conversationRepository.countBySessionId(sessionId)
        );
    }

    @Override
    public SessionDto endSession(Long sessionId) {

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Session not found with id : "
                                        + sessionId));

        session.setStatus(SessionStatus.COMPLETED);
        session.setEndTime(LocalDateTime.now());

        Session savedSession =
                sessionRepository.save(session);

        return SessionMapper.toDto(savedSession);
    }

    @Override
    public SessionReportDto getSessionReport(Long sessionId) {

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Session not found with id : "
                                        + sessionId));

        List<Conversation> conversations =
                conversationRepository.findBySessionId(sessionId);

        StringBuilder history = new StringBuilder();

        for (Conversation conversation : conversations) {

            history.append("Question: ")
                    .append(conversation.getAiQuestion())
                    .append("\n");

            history.append("Answer: ")
                    .append(conversation.getUserAnswer())
                    .append("\n");

            history.append("Feedback: ")
                    .append(conversation.getAiFeedback())
                    .append("\n\n");
        }

        SessionReportDto report =
                aiService.generateSessionReport(
                        history.toString(),session.getMode());

        report.setSessionId(session.getId());

        return report;
    }
}