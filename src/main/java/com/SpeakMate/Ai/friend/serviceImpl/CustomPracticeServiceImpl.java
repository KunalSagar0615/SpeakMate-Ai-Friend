package com.SpeakMate.Ai.friend.serviceImpl;

import com.SpeakMate.Ai.friend.dto.*;
import com.SpeakMate.Ai.friend.entities.CustomPracticeAttempt;
import com.SpeakMate.Ai.friend.entities.CustomPracticeQuestion;
import com.SpeakMate.Ai.friend.entities.CustomPracticeSession;
import com.SpeakMate.Ai.friend.entities.User;
import com.SpeakMate.Ai.friend.enumeration.AnswerEvaluationStatus;
import com.SpeakMate.Ai.friend.enumeration.CustomPracticeStatus;
import com.SpeakMate.Ai.friend.enumeration.PracticeAttemptType;
import com.SpeakMate.Ai.friend.exception.ResourceNotFoundException;
import com.SpeakMate.Ai.friend.repository.CustomPracticeAttemptRepository;
import com.SpeakMate.Ai.friend.repository.CustomPracticeQuestionRepository;
import com.SpeakMate.Ai.friend.repository.CustomPracticeSessionRepository;
import com.SpeakMate.Ai.friend.repository.UserRepository;
import com.SpeakMate.Ai.friend.service.AiService;
import com.SpeakMate.Ai.friend.service.CustomPracticeReportService;
import com.SpeakMate.Ai.friend.service.CustomPracticeService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CustomPracticeServiceImpl
        implements CustomPracticeService {

    private static final int DEFAULT_PAUSE_DAYS = 15;
    private static final int MAX_PAUSE_DAYS = 30;
    private static final int MAX_QUESTIONS = 100;

    private final CustomPracticeSessionRepository sessionRepository;
    private final CustomPracticeQuestionRepository questionRepository;
    private final CustomPracticeAttemptRepository attemptRepository;
    private final UserRepository userRepository;
    private final AiService aiService;
    private final CustomPracticeReportService reportService;

    public CustomPracticeServiceImpl(
            CustomPracticeSessionRepository sessionRepository,
            CustomPracticeQuestionRepository questionRepository,
            CustomPracticeAttemptRepository attemptRepository,
            UserRepository userRepository,
            AiService aiService,
            CustomPracticeReportService reportService) {

        this.sessionRepository = sessionRepository;
        this.questionRepository = questionRepository;
        this.attemptRepository = attemptRepository;
        this.userRepository = userRepository;
        this.aiService = aiService;
        this.reportService = reportService;
    }

    // =========================================================
    // QUESTION EXTRACTION
    // =========================================================

    @Override
    public ExtractQuestionsResponseDto extractQuestions(
            ExtractQuestionsRequestDto request) {

        if (request == null
                || request.getContent() == null
                || request.getContent().isBlank()) {

            throw new IllegalArgumentException(
                    "Question content cannot be empty."
            );
        }

        AiQuestionExtractionDto extraction =
                aiService.extractCustomPracticeQuestions(
                        request.getContent()
                );

        if (extraction == null
                || extraction.getQuestions() == null
                || extraction.getQuestions().isEmpty()) {

            throw new IllegalStateException(
                    "No questions could be extracted."
            );
        }

        List<String> questions =
                extraction.getQuestions()
                        .stream()
                        .filter(question ->
                                question != null
                                        && !question.isBlank())
                        .map(String::trim)
                        .toList();

        if (questions.isEmpty()) {
            throw new IllegalStateException(
                    "No valid questions could be extracted."
            );
        }

        if (questions.size() > MAX_QUESTIONS) {
            throw new IllegalArgumentException(
                    "A maximum of 100 questions is allowed."
            );
        }

        return new ExtractQuestionsResponseDto(
                questions,
                questions.size()
        );
    }

    // =========================================================
    // CREATE SESSION
    // =========================================================

    @Override
    @Transactional
    public CustomPracticeSessionDto createSession(
            CreateCustomPracticeRequestDto request) {

        User user = getAuthenticatedUser();

        validateCreateRequest(request);

        List<String> cleanedQuestions =
                request.getQuestions()
                        .stream()
                        .map(String::trim)
                        .toList();

        CustomPracticeSession session =
                new CustomPracticeSession();

        session.setSessionName(
                request.getSessionName().trim()
        );

        session.setStatus(
                CustomPracticeStatus.ACTIVE
        );

        session.setTotalQuestions(
                cleanedQuestions.size()
        );

        session.setCurrentQuestionIndex(0);
        session.setCurrentRound(1);
        session.setStartedAt(LocalDateTime.now());
        session.setUser(user);

        List<CustomPracticeQuestion> questionEntities =
                new ArrayList<>();

        for (int i = 0;
             i < cleanedQuestions.size();
             i++) {

            CustomPracticeQuestion question =
                    new CustomPracticeQuestion();

            question.setQuestionText(
                    cleanedQuestions.get(i)
            );

            question.setQuestionOrder(i + 1);

            question.setFirstRoundSkipped(false);
            question.setRetryRequired(false);
            question.setRetryCompleted(false);
            question.setCompleted(false);
            question.setSession(session);

            questionEntities.add(question);
        }

        session.setQuestions(questionEntities);

        CustomPracticeSession savedSession =
                sessionRepository.save(session);

        return buildSessionDto(savedSession);
    }

    // =========================================================
    // SUBMIT ANSWER
    // =========================================================

    @Override
    @Transactional
    public AnswerEvaluationResponseDto submitAnswer(
            Long sessionId,
            SubmitCustomAnswerRequestDto request) {

        if (request == null
                || request.getQuestionId() == null
                || request.getAnswer() == null
                || request.getAnswer().isBlank()) {

            throw new IllegalArgumentException(
                    "Question ID and answer are required."
            );
        }

        CustomPracticeSession session =
                getOwnedSessionForUpdate(sessionId);

        ensureSessionActive(session);

        CustomPracticeQuestion currentQuestion =
                getCurrentQuestion(session);

        if (!currentQuestion.getId()
                .equals(request.getQuestionId())) {

            throw new IllegalStateException(
                    "Only the current question can be answered."
            );
        }

        if (Boolean.TRUE.equals(
                currentQuestion.getCompleted())) {

            throw new IllegalStateException(
                    "This question has already been completed."
            );
        }

        int attemptNumber =
                session.getCurrentRound() == 1
                        ? 1
                        : determineSecondRoundAttemptNumber(
                        currentQuestion
                );

        if (attemptRepository
                .existsByQuestionAndAttemptNumber(
                        currentQuestion,
                        attemptNumber
                )) {

            throw new IllegalStateException(
                    "This answer attempt has already been submitted."
            );
        }

        AiAnswerEvaluationDto evaluation =
                aiService.evaluateCustomPracticeAnswer(
                        currentQuestion.getQuestionText(),
                        request.getAnswer().trim()
                );

        validateAiEvaluation(evaluation);

        PracticeAttemptType attemptType =
                session.getCurrentRound() == 1
                        ? PracticeAttemptType.FIRST_ATTEMPT
                        : PracticeAttemptType.SECOND_ATTEMPT;

        CustomPracticeAttempt attempt =
                new CustomPracticeAttempt();

        attempt.setAttemptNumber(attemptNumber);
        attempt.setAttemptType(attemptType);
        attempt.setUserAnswer(
                request.getAnswer().trim()
        );
        attempt.setEvaluationStatus(
                evaluation.getStatus()
        );
        attempt.setScore(
                evaluation.getScore()
        );
        attempt.setFeedback(
                evaluation.getFeedback().trim()
        );
        attempt.setQuestion(currentQuestion);

        attemptRepository.save(attempt);

        currentQuestion.setDraftAnswer(null);

        boolean retryRequired = false;

        if (session.getCurrentRound() == 1) {

            if (evaluation.getStatus()
                    == AnswerEvaluationStatus.INCORRECT) {

                currentQuestion.setRetryRequired(true);
                currentQuestion.setRetryCompleted(false);
                currentQuestion.setCompleted(false);

                retryRequired = true;

            } else {

                currentQuestion.setRetryRequired(false);
                currentQuestion.setRetryCompleted(false);
                currentQuestion.setCompleted(true);
            }

        } else {

            currentQuestion.setRetryRequired(false);
            currentQuestion.setRetryCompleted(true);
            currentQuestion.setCompleted(true);
        }

        questionRepository.save(currentQuestion);

        return new AnswerEvaluationResponseDto(
                currentQuestion.getId(),
                attemptNumber,
                evaluation.getStatus(),
                evaluation.getScore(),
                evaluation.getFeedback(),
                retryRequired
        );
    }

    // =========================================================
    // SKIP CURRENT QUESTION
    // =========================================================

    @Override
    @Transactional
    public SkipQuestionResponseDto skipCurrentQuestion(
            Long sessionId) {

        CustomPracticeSession session =
                getOwnedSessionForUpdate(sessionId);

        ensureSessionActive(session);

        CustomPracticeQuestion currentQuestion =
                getCurrentQuestion(session);

        if (Boolean.TRUE.equals(
                currentQuestion.getCompleted())) {

            throw new IllegalStateException(
                    "This question has already been completed."
            );
        }

        currentQuestion.setDraftAnswer(null);

        if (session.getCurrentRound() == 1) {

            currentQuestion.setFirstRoundSkipped(true);
            currentQuestion.setRetryRequired(true);
            currentQuestion.setRetryCompleted(false);
            currentQuestion.setCompleted(false);

            questionRepository.save(currentQuestion);

            return new SkipQuestionResponseDto(
                    currentQuestion.getId(),
                    currentQuestion.getQuestionOrder(),
                    1,
                    true,
                    false,
                    "Question skipped. You will get one more attempt after the first round."
            );
        }

        currentQuestion.setRetryRequired(false);
        currentQuestion.setRetryCompleted(true);
        currentQuestion.setCompleted(true);

        questionRepository.save(currentQuestion);

        return new SkipQuestionResponseDto(
                currentQuestion.getId(),
                currentQuestion.getQuestionOrder(),
                2,
                false,
                true,
                "Question skipped and marked as skipped for this session."
        );
    }

    // =========================================================
    // NEXT QUESTION
    // =========================================================

    @Override
    @Transactional
    public NextQuestionResponseDto getNextQuestion(
            Long sessionId) {

        CustomPracticeSession session =
                getOwnedSessionForUpdate(sessionId);

        ensureSessionActive(session);

        CustomPracticeQuestion currentQuestion =
                getCurrentQuestion(session);

        if (session.getCurrentRound() == 1) {

            if (!isRoundOneQuestionProcessed(
                    currentQuestion)) {

                throw new IllegalStateException(
                        "Submit or skip the current question before continuing."
                );
            }

            List<CustomPracticeQuestion> questions =
                    getOrderedQuestions(session);

            int nextIndex =
                    session.getCurrentQuestionIndex() + 1;

            if (nextIndex < questions.size()) {

                session.setCurrentQuestionIndex(
                        nextIndex
                );

                sessionRepository.save(session);

                return new NextQuestionResponseDto(
                        session.getId(),
                        session.getStatus(),
                        1,
                        false,
                        false,
                        buildQuestionDto(
                                session,
                                questions.get(nextIndex)
                        ),
                        null
                );
            }

            List<CustomPracticeQuestion> retryQuestions =
                    getPendingRetryQuestions(session);

            if (!retryQuestions.isEmpty()) {

                session.setCurrentRound(2);

                CustomPracticeQuestion firstRetry =
                        retryQuestions.get(0);

                session.setCurrentQuestionIndex(
                        findQuestionIndex(
                                questions,
                                firstRetry
                        )
                );

                sessionRepository.save(session);

                return new NextQuestionResponseDto(
                        session.getId(),
                        session.getStatus(),
                        2,
                        true,
                        false,
                        buildQuestionDto(
                                session,
                                firstRetry
                        ),
                        "First round completed. Retry round has started."
                );
            }

            return completeSession(session);
        }

        // =====================================================
        // ROUND 2
        // =====================================================

        if (!Boolean.TRUE.equals(
                currentQuestion.getRetryCompleted())) {

            throw new IllegalStateException(
                    "Submit or skip the current retry question before continuing."
            );
        }

        List<CustomPracticeQuestion> retryQuestions =
                getPendingRetryQuestions(session);

        if (!retryQuestions.isEmpty()) {

            List<CustomPracticeQuestion> allQuestions =
                    getOrderedQuestions(session);

            CustomPracticeQuestion nextRetry =
                    retryQuestions.get(0);

            session.setCurrentQuestionIndex(
                    findQuestionIndex(
                            allQuestions,
                            nextRetry
                    )
            );

            sessionRepository.save(session);

            return new NextQuestionResponseDto(
                    session.getId(),
                    session.getStatus(),
                    2,
                    false,
                    false,
                    buildQuestionDto(
                            session,
                            nextRetry
                    ),
                    null
            );
        }

        return completeSession(session);
    }

    // =========================================================
    // SAVE DRAFT
    // =========================================================

    @Override
    @Transactional
    public void saveDraftAnswer(
            Long sessionId,
            SaveDraftAnswerRequestDto request) {

        if (request == null
                || request.getQuestionId() == null) {

            throw new IllegalArgumentException(
                    "Question ID is required."
            );
        }

        CustomPracticeSession session =
                getOwnedSessionForUpdate(sessionId);

        ensureSessionActive(session);

        CustomPracticeQuestion currentQuestion =
                getCurrentQuestion(session);

        if (!currentQuestion.getId()
                .equals(request.getQuestionId())) {

            throw new IllegalStateException(
                    "Draft can only be saved for the current question."
            );
        }

        if (Boolean.TRUE.equals(
                currentQuestion.getCompleted())) {

            throw new IllegalStateException(
                    "Draft cannot be saved for a completed question."
            );
        }

        String draft =
                request.getDraftAnswer();

        if (draft == null || draft.isBlank()) {
            currentQuestion.setDraftAnswer(null);
        } else {
            currentQuestion.setDraftAnswer(
                    draft.trim()
            );
        }

        questionRepository.save(currentQuestion);
    }

    // =========================================================
    // CUSTOM PAUSE
    // =========================================================

    @Override
    @Transactional
    public CustomPracticeSessionDto pauseSession(
            Long sessionId,
            PauseCustomPracticeRequestDto request) {

        if (request == null
                || request.getPauseDays() == null) {

            throw new IllegalArgumentException(
                    "Pause duration is required."
            );
        }

        int pauseDays =
                request.getPauseDays();

        if (pauseDays < 1
                || pauseDays > MAX_PAUSE_DAYS) {

            throw new IllegalArgumentException(
                    "Pause duration must be between 1 and 30 days."
            );
        }

        return pauseSessionInternal(
                sessionId,
                pauseDays
        );
    }

    // =========================================================
    // DEFAULT 15-DAY PAUSE
    // =========================================================

    @Override
    @Transactional
    public CustomPracticeSessionDto
    pauseSessionWithDefaultDuration(
            Long sessionId) {

        return pauseSessionInternal(
                sessionId,
                DEFAULT_PAUSE_DAYS
        );
    }

    private CustomPracticeSessionDto pauseSessionInternal(
            Long sessionId,
            int pauseDays) {

        CustomPracticeSession session =
                getOwnedSessionForUpdate(sessionId);

        ensureSessionActive(session);

        LocalDateTime now =
                LocalDateTime.now();

        session.setStatus(
                CustomPracticeStatus.PAUSED
        );

        session.setPausedAt(now);

        session.setExpiresAt(
                now.plusDays(pauseDays)
        );

        CustomPracticeSession saved =
                sessionRepository.save(session);

        return buildSessionDto(saved);
    }

    // =========================================================
    // RESUME SESSION
    // =========================================================

    @Override
    @Transactional
    public CustomPracticeSessionDto resumeSession(
            Long sessionId) {

        CustomPracticeSession session =
                getOwnedSessionForUpdate(sessionId);

        expireIfNecessary(session);

        if (session.getStatus()
                == CustomPracticeStatus.EXPIRED) {

            throw new IllegalStateException(
                    "This practice session has expired."
            );
        }

        if (session.getStatus()
                != CustomPracticeStatus.PAUSED) {

            throw new IllegalStateException(
                    "Only paused sessions can be resumed."
            );
        }

        session.setStatus(
                CustomPracticeStatus.ACTIVE
        );

        session.setPausedAt(null);
        session.setExpiresAt(null);

        CustomPracticeSession saved =
                sessionRepository.save(session);

        return buildSessionDto(saved);
    }

    // =========================================================
    // END SESSION
    // =========================================================

    @Override
    @Transactional
    public EndCustomPracticeResponseDto endSession(
            Long sessionId) {

        CustomPracticeSession session =
                getOwnedSessionForUpdate(sessionId);

        expireIfNecessary(session);

        if (session.getStatus()
                == CustomPracticeStatus.COMPLETED) {

            throw new IllegalStateException(
                    "Completed sessions cannot be ended."
            );
        }

        if (session.getStatus()
                == CustomPracticeStatus.ENDED) {

            throw new IllegalStateException(
                    "This session has already been ended."
            );
        }

        if (session.getStatus()
                == CustomPracticeStatus.EXPIRED) {

            throw new IllegalStateException(
                    "Expired sessions cannot be ended."
            );
        }

        LocalDateTime now =
                LocalDateTime.now();

        session.setStatus(
                CustomPracticeStatus.ENDED
        );

        session.setEndedAt(now);
        session.setPausedAt(null);
        session.setExpiresAt(null);

        sessionRepository.save(session);

        reportService.generateAndStoreReport(session);

        QuestionCounts counts =
                calculateQuestionCounts(session);

        return new EndCustomPracticeResponseDto(
                session.getId(),
                session.getStatus(),
                session.getTotalQuestions(),
                counts.answered(),
                counts.skipped(),
                counts.notAttempted(),
                session.getEndedAt(),
                true,
                "Practice session ended. Your partial report is now available."
        );
    }

    // =========================================================
    // PAUSED SESSION LIST
    // =========================================================

    @Override
    @Transactional
    public List<CustomPracticeSummaryDto> getPausedSessions() {

        User user = getAuthenticatedUser();

        List<CustomPracticeSession> pausedSessions =
                sessionRepository
                        .findAllByUserAndStatusOrderByStartedAtDesc(
                                user,
                                CustomPracticeStatus.PAUSED
                        );

        List<CustomPracticeSummaryDto> result =
                new ArrayList<>();

        for (CustomPracticeSession session :
                pausedSessions) {

            expireIfNecessary(session);

            if (session.getStatus()
                    == CustomPracticeStatus.PAUSED) {

                result.add(
                        buildSummaryDto(session)
                );
            }
        }

        return result;
    }

    // =========================================================
    // SESSION HISTORY
    // =========================================================

    @Override
    @Transactional
    public List<CustomPracticeSummaryDto> getSessionHistory() {

        User user = getAuthenticatedUser();

        List<CustomPracticeSession> pausedSessions =
                sessionRepository
                        .findAllByUserAndStatusOrderByStartedAtDesc(
                                user,
                                CustomPracticeStatus.PAUSED
                        );

        for (CustomPracticeSession session :
                pausedSessions) {

            expireIfNecessary(session);
        }

        List<CustomPracticeStatus> historyStatuses =
                List.of(
                        CustomPracticeStatus.COMPLETED,
                        CustomPracticeStatus.ENDED,
                        CustomPracticeStatus.EXPIRED
                );

        List<CustomPracticeSession> history =
                sessionRepository
                        .findAllByUserAndStatusInOrderByStartedAtDesc(
                                user,
                                historyStatuses
                        );

        return history.stream()
                .map(this::buildSummaryDto)
                .toList();
    }

    // =========================================================
    // GET SESSION
    // =========================================================

    @Override
    @Transactional
    public CustomPracticeSessionDto getSession(
            Long sessionId) {

        CustomPracticeSession session =
                getOwnedSession(sessionId);

        expireIfNecessary(session);

        return buildSessionDto(session);
    }

    // =========================================================
    // AUTOMATIC COMPLETION
    // =========================================================

    private NextQuestionResponseDto completeSession(
            CustomPracticeSession session) {

        session.setStatus(
                CustomPracticeStatus.COMPLETED
        );

        session.setCompletedAt(
                LocalDateTime.now()
        );

        session.setPausedAt(null);
        session.setExpiresAt(null);

        sessionRepository.save(session);

        reportService.generateAndStoreReport(session);

        return new NextQuestionResponseDto(
                session.getId(),
                CustomPracticeStatus.COMPLETED,
                session.getCurrentRound(),
                false,
                true,
                null,
                "Your questions are over! You can now check your report."
        );
    }

    // =========================================================
    // EXPIRATION
    // =========================================================

    private void expireIfNecessary(
            CustomPracticeSession session) {

        if (session.getStatus()
                != CustomPracticeStatus.PAUSED) {
            return;
        }

        if (session.getExpiresAt() == null) {
            return;
        }

        LocalDateTime now =
                LocalDateTime.now();

        if (now.isBefore(
                session.getExpiresAt())) {
            return;
        }

        session.setStatus(
                CustomPracticeStatus.EXPIRED
        );

        session.setExpiredAt(now);

        sessionRepository.save(session);

        reportService.generateAndStoreReport(session);
    }

    // =========================================================
    // SESSION DTO
    // =========================================================

    private CustomPracticeSessionDto buildSessionDto(
            CustomPracticeSession session) {

        List<CustomPracticeQuestion> questions =
                getOrderedQuestions(session);

        int answered =
                countAnsweredQuestions(questions);

        int remaining =
                Math.max(
                        session.getTotalQuestions()
                                - answered,
                        0
                );

        CustomPracticeQuestionDto currentQuestionDto =
                null;

        if ((session.getStatus()
                == CustomPracticeStatus.ACTIVE
                || session.getStatus()
                == CustomPracticeStatus.PAUSED)
                && !questions.isEmpty()) {

            int index =
                    session.getCurrentQuestionIndex();

            if (index >= 0
                    && index < questions.size()) {

                currentQuestionDto =
                        buildQuestionDto(
                                session,
                                questions.get(index)
                        );
            }
        }

        return new CustomPracticeSessionDto(
                session.getId(),
                session.getSessionName(),
                session.getStatus(),
                session.getTotalQuestions(),
                session.getCurrentRound(),
                answered,
                remaining,
                session.getStartedAt(),
                session.getPausedAt(),
                session.getExpiresAt(),
                currentQuestionDto
        );
    }

    private CustomPracticeSummaryDto buildSummaryDto(
            CustomPracticeSession session) {

        List<CustomPracticeQuestion> questions =
                getOrderedQuestions(session);

        int answered =
                countAnsweredQuestions(questions);

        return new CustomPracticeSummaryDto(
                session.getId(),
                session.getSessionName(),
                session.getStatus(),
                session.getTotalQuestions(),
                answered,
                Math.max(
                        session.getTotalQuestions()
                                - answered,
                        0
                ),
                session.getCurrentRound(),
                session.getStartedAt(),
                session.getPausedAt(),
                session.getExpiresAt()
        );
    }

    // =========================================================
    // QUESTION DTO
    // =========================================================

    private CustomPracticeQuestionDto buildQuestionDto(
            CustomPracticeSession session,
            CustomPracticeQuestion question) {

        return new CustomPracticeQuestionDto(
                question.getId(),
                question.getQuestionText(),
                question.getQuestionOrder(),
                session.getTotalQuestions(),
                session.getCurrentRound(),
                session.getCurrentRound() == 2,
                question.getDraftAnswer()
        );
    }

    // =========================================================
    // CURRENT QUESTION
    // =========================================================

    private CustomPracticeQuestion getCurrentQuestion(
            CustomPracticeSession session) {

        List<CustomPracticeQuestion> questions =
                getOrderedQuestions(session);

        if (questions.isEmpty()) {
            throw new IllegalStateException(
                    "This practice session contains no questions."
            );
        }

        int index =
                session.getCurrentQuestionIndex();

        if (index < 0
                || index >= questions.size()) {

            throw new IllegalStateException(
                    "Invalid current question position."
            );
        }

        return questions.get(index);
    }

    private List<CustomPracticeQuestion> getOrderedQuestions(
            CustomPracticeSession session) {

        return questionRepository
                .findAllBySessionOrderByQuestionOrderAsc(
                        session
                );
    }

    // =========================================================
    // RETRY QUESTIONS
    // =========================================================

    private List<CustomPracticeQuestion> getPendingRetryQuestions(
            CustomPracticeSession session) {

        return questionRepository
                .findAllBySessionAndRetryRequiredTrueAndRetryCompletedFalseOrderByQuestionOrderAsc(
                        session
                );
    }

    private int findQuestionIndex(
            List<CustomPracticeQuestion> questions,
            CustomPracticeQuestion target) {

        for (int i = 0;
             i < questions.size();
             i++) {

            if (questions.get(i)
                    .getId()
                    .equals(target.getId())) {

                return i;
            }
        }

        throw new IllegalStateException(
                "Question does not belong to this session."
        );
    }

    // =========================================================
    // ATTEMPT NUMBER
    // =========================================================

    private int determineSecondRoundAttemptNumber(
            CustomPracticeQuestion question) {

        List<CustomPracticeAttempt> attempts =
                attemptRepository
                        .findAllByQuestionOrderByAttemptNumberAsc(
                                question
                        );

        if (attempts.isEmpty()) {
            return 1;
        }

        return 2;
    }

    // =========================================================
    // ROUND 1 PROCESSING CHECK
    // =========================================================

    private boolean isRoundOneQuestionProcessed(
            CustomPracticeQuestion question) {

        if (Boolean.TRUE.equals(
                question.getFirstRoundSkipped())) {

            return true;
        }

        return attemptRepository
                .existsByQuestionAndAttemptNumber(
                        question,
                        1
                );
    }

    // =========================================================
    // COUNTS
    // =========================================================

    private int countAnsweredQuestions(
            List<CustomPracticeQuestion> questions) {

        int answered = 0;

        for (CustomPracticeQuestion question :
                questions) {

            if (!attemptRepository
                    .findAllByQuestionOrderByAttemptNumberAsc(
                            question
                    )
                    .isEmpty()) {

                answered++;
            }
        }

        return answered;
    }

    private QuestionCounts calculateQuestionCounts(
            CustomPracticeSession session) {

        List<CustomPracticeQuestion> questions =
                getOrderedQuestions(session);

        int answered = 0;
        int skipped = 0;
        int notAttempted = 0;

        for (CustomPracticeQuestion question :
                questions) {

            List<CustomPracticeAttempt> attempts =
                    attemptRepository
                            .findAllByQuestionOrderByAttemptNumberAsc(
                                    question
                            );

            if (!attempts.isEmpty()) {

                answered++;
                continue;
            }

            if (isPermanentlySkipped(question)) {
                skipped++;
            } else {
                notAttempted++;
            }
        }

        return new QuestionCounts(
                answered,
                skipped,
                notAttempted
        );
    }

    private boolean isPermanentlySkipped(
            CustomPracticeQuestion question) {

        return Boolean.TRUE.equals(
                question.getFirstRoundSkipped())
                && Boolean.TRUE.equals(
                question.getRetryCompleted())
                && attemptRepository
                .findAllByQuestionOrderByAttemptNumberAsc(
                        question
                )
                .isEmpty();
    }

    // =========================================================
    // VALIDATION
    // =========================================================

    private void validateCreateRequest(
            CreateCustomPracticeRequestDto request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Practice session request is required."
            );
        }

        if (request.getSessionName() == null
                || request.getSessionName()
                .isBlank()) {

            throw new IllegalArgumentException(
                    "Session name cannot be empty."
            );
        }

        if (request.getSessionName()
                .trim()
                .length() > 150) {

            throw new IllegalArgumentException(
                    "Session name cannot exceed 150 characters."
            );
        }

        if (request.getQuestions() == null
                || request.getQuestions().isEmpty()) {

            throw new IllegalArgumentException(
                    "At least one question is required."
            );
        }

        if (request.getQuestions().size()
                > MAX_QUESTIONS) {

            throw new IllegalArgumentException(
                    "A maximum of 100 questions is allowed."
            );
        }

        for (String question :
                request.getQuestions()) {

            if (question == null
                    || question.isBlank()) {

                throw new IllegalArgumentException(
                        "Questions cannot be empty."
                );
            }
        }
    }

    private void validateAiEvaluation(
            AiAnswerEvaluationDto evaluation) {

        if (evaluation == null) {
            throw new IllegalStateException(
                    "AI evaluation was not returned."
            );
        }

        if (evaluation.getStatus() == null) {
            throw new IllegalStateException(
                    "AI evaluation status is missing."
            );
        }

        if (evaluation.getScore() == null
                || evaluation.getScore() < 0
                || evaluation.getScore() > 100) {

            throw new IllegalStateException(
                    "AI evaluation score is invalid."
            );
        }

        if (evaluation.getFeedback() == null
                || evaluation.getFeedback()
                .isBlank()) {

            throw new IllegalStateException(
                    "AI evaluation feedback is missing."
            );
        }
    }

    private void ensureSessionActive(
            CustomPracticeSession session) {

        expireIfNecessary(session);

        if (session.getStatus()
                != CustomPracticeStatus.ACTIVE) {

            throw new IllegalStateException(
                    "This practice session is not active."
            );
        }
    }

    // =========================================================
    // OWNERSHIP + AUTHENTICATION
    // =========================================================

    private CustomPracticeSession getOwnedSession(
            Long sessionId) {

        validateSessionId(sessionId);

        User user =
                getAuthenticatedUser();

        return sessionRepository
                .findByIdAndUser(
                        sessionId,
                        user
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Custom practice session not found."
                        )
                );
    }

    /**
     * Used by state-changing operations.
     *
     * The repository applies PESSIMISTIC_WRITE to the session row,
     * preventing concurrent requests from modifying the same practice
     * session state at the same time.
     */
    private CustomPracticeSession getOwnedSessionForUpdate(
            Long sessionId) {

        validateSessionId(sessionId);

        User user =
                getAuthenticatedUser();

        return sessionRepository
                .findByIdAndUserForUpdate(
                        sessionId,
                        user
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Custom practice session not found."
                        )
                );
    }

    private void validateSessionId(
            Long sessionId) {

        if (sessionId == null) {
            throw new IllegalArgumentException(
                    "Session ID is required."
            );
        }
    }

    private User getAuthenticatedUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().isBlank()
                || "anonymousUser".equals(
                authentication.getName())) {

            throw new IllegalStateException(
                    "Authenticated user not found."
            );
        }

        String username =
                authentication.getName();

        return userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Authenticated user does not exist."
                        )
                );
    }

    // =========================================================
    // INTERNAL RECORDS
    // =========================================================

    private record QuestionCounts(
            int answered,
            int skipped,
            int notAttempted
    ) {
    }
}