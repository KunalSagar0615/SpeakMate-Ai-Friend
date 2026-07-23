package com.SpeakMate.Ai.friend.serviceImpl;

import com.SpeakMate.Ai.friend.dto.CustomPracticeAttemptDto;
import com.SpeakMate.Ai.friend.dto.CustomPracticeQuestionReportDto;
import com.SpeakMate.Ai.friend.dto.CustomPracticeReportDto;
import com.SpeakMate.Ai.friend.entities.CustomPracticeAttempt;
import com.SpeakMate.Ai.friend.entities.CustomPracticeQuestion;
import com.SpeakMate.Ai.friend.entities.CustomPracticeSession;
import com.SpeakMate.Ai.friend.entities.User;
import com.SpeakMate.Ai.friend.enumeration.CustomPracticeStatus;
import com.SpeakMate.Ai.friend.exception.ResourceNotFoundException;
import com.SpeakMate.Ai.friend.repository.CustomPracticeAttemptRepository;
import com.SpeakMate.Ai.friend.repository.CustomPracticeQuestionRepository;
import com.SpeakMate.Ai.friend.repository.CustomPracticeSessionRepository;
import com.SpeakMate.Ai.friend.repository.UserRepository;
import com.SpeakMate.Ai.friend.service.AiService;
import com.SpeakMate.Ai.friend.service.CustomPracticeReportService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CustomPracticeReportServiceImpl
        implements CustomPracticeReportService {

    private final CustomPracticeSessionRepository sessionRepository;
    private final CustomPracticeQuestionRepository questionRepository;
    private final CustomPracticeAttemptRepository attemptRepository;
    private final UserRepository userRepository;
    private final AiService aiService;

    public CustomPracticeReportServiceImpl(
            CustomPracticeSessionRepository sessionRepository,
            CustomPracticeQuestionRepository questionRepository,
            CustomPracticeAttemptRepository attemptRepository,
            UserRepository userRepository,
            AiService aiService) {

        this.sessionRepository = sessionRepository;
        this.questionRepository = questionRepository;
        this.attemptRepository = attemptRepository;
        this.userRepository = userRepository;
        this.aiService = aiService;
    }

    // =========================================================
    // GENERATE + STORE REPORT
    // =========================================================

    @Override
    @Transactional
    public void generateAndStoreReport(
            CustomPracticeSession session) {

        if (session == null || session.getId() == null) {
            throw new IllegalArgumentException(
                    "Practice session is required."
            );
        }

        if (!isReportableStatus(session.getStatus())) {
            throw new IllegalStateException(
                    "Report can only be generated for completed, ended, or expired sessions."
            );
        }

        /*
         * Idempotency protection.
         *
         * The scheduler, access-time expiration check and report
         * endpoint can potentially reach report generation at
         * different times. Once both values exist, Groq should
         * not be called again.
         */
        if (session.getOverallScore() != null
                && session.getOverallAiFeedback() != null
                && !session.getOverallAiFeedback().isBlank()) {

            return;
        }

        List<CustomPracticeQuestion> questions =
                getOrderedQuestions(session);

        ReportStatistics statistics =
                calculateStatistics(questions);

        double overallScore =
                calculateOverallScore(questions);

        String practiceSummary =
                buildPracticeSummary(
                        session,
                        questions,
                        statistics,
                        overallScore
                );

        String overallFeedback;

        try {

            overallFeedback =
                    aiService
                            .generateCustomPracticeOverallFeedback(
                                    practiceSummary
                            );

            if (overallFeedback == null
                    || overallFeedback.isBlank()) {

                overallFeedback =
                        buildFallbackFeedback();
            }

        } catch (Exception e) {

            /*
             * A Groq outage must not prevent the deterministic
             * report from being stored.
             */
            overallFeedback =
                    buildFallbackFeedback();
        }

        session.setOverallScore(overallScore);
        session.setOverallAiFeedback(
                overallFeedback.trim()
        );

        sessionRepository.save(session);
    }

    // =========================================================
    // GET REPORT
    // =========================================================

    @Override
    @Transactional
    public CustomPracticeReportDto getReport(
            Long sessionId) {

        if (sessionId == null) {
            throw new IllegalArgumentException(
                    "Session ID is required."
            );
        }

        User user =
                getAuthenticatedUser();

        CustomPracticeSession session =
                sessionRepository
                        .findByIdAndUser(
                                sessionId,
                                user
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Custom practice session not found."
                                )
                        );

        if (!isReportableStatus(
                session.getStatus())) {

            throw new IllegalStateException(
                    "Report is not available while the practice session is still active or paused."
            );
        }

        if (session.getOverallScore() == null
                || session.getOverallAiFeedback() == null
                || session.getOverallAiFeedback().isBlank()) {

            generateAndStoreReport(session);
        }

        List<CustomPracticeQuestion> questions =
                getOrderedQuestions(session);

        ReportStatistics statistics =
                calculateStatistics(questions);

        List<CustomPracticeQuestionReportDto>
                questionReports =
                new ArrayList<>();

        for (CustomPracticeQuestion question :
                questions) {

            questionReports.add(
                    buildQuestionReport(question)
            );
        }

        return new CustomPracticeReportDto(
                session.getId(),
                session.getSessionName(),
                session.getStatus(),
                session.getTotalQuestions(),
                statistics.answeredQuestions(),
                statistics.correctQuestions(),
                statistics.partiallyCorrectQuestions(),
                statistics.incorrectQuestions(),
                statistics.skippedQuestions(),
                statistics.notAttemptedQuestions(),
                statistics.retriedQuestions(),
                session.getOverallScore(),
                session.getOverallAiFeedback(),
                session.getStartedAt(),
                session.getCompletedAt(),
                session.getEndedAt(),
                session.getExpiredAt(),
                calculatePracticeDurationSeconds(
                        session
                ),
                questionReports
        );
    }

    // =========================================================
    // STATISTICS
    // =========================================================

    private ReportStatistics calculateStatistics(
            List<CustomPracticeQuestion> questions) {

        int answered = 0;
        int correct = 0;
        int partiallyCorrect = 0;
        int incorrect = 0;
        int skipped = 0;
        int notAttempted = 0;
        int retried = 0;

        for (CustomPracticeQuestion question :
                questions) {

            List<CustomPracticeAttempt> attempts =
                    getAttempts(question);

            if (wasRetried(question, attempts)) {
                retried++;
            }

            CustomPracticeAttempt scoringAttempt =
                    getScoringAttempt(attempts);

            if (scoringAttempt != null) {

                answered++;

                switch (
                        scoringAttempt
                                .getEvaluationStatus()) {

                    case CORRECT ->
                            correct++;

                    case PARTIALLY_CORRECT ->
                            partiallyCorrect++;

                    case INCORRECT ->
                            incorrect++;
                }

                continue;
            }

            if (isPermanentlySkipped(
                    question,
                    attempts)) {

                skipped++;

            } else {

                notAttempted++;
            }
        }

        return new ReportStatistics(
                answered,
                correct,
                partiallyCorrect,
                incorrect,
                skipped,
                notAttempted,
                retried
        );
    }

    // =========================================================
    // OVERALL SCORE
    // =========================================================

    private double calculateOverallScore(
            List<CustomPracticeQuestion> questions) {

        long totalScore = 0;
        int scoredQuestions = 0;

        for (CustomPracticeQuestion question :
                questions) {

            List<CustomPracticeAttempt> attempts =
                    getAttempts(question);

            CustomPracticeAttempt scoringAttempt =
                    getScoringAttempt(attempts);

            if (scoringAttempt != null) {

                totalScore +=
                        scoringAttempt.getScore();

                scoredQuestions++;
            }
        }

        if (scoredQuestions == 0) {
            return 0.0;
        }

        double score =
                (double) totalScore
                        / scoredQuestions;

        return Math.round(
                score * 100.0
        ) / 100.0;
    }

    // =========================================================
    // QUESTION REPORT
    // =========================================================

    private CustomPracticeQuestionReportDto
    buildQuestionReport(
            CustomPracticeQuestion question) {

        List<CustomPracticeAttempt> attempts =
                getAttempts(question);

        List<CustomPracticeAttemptDto> attemptDtos =
                attempts.stream()
                        .map(this::mapAttempt)
                        .toList();

        CustomPracticeAttempt scoringAttempt =
                getScoringAttempt(attempts);

        boolean answered =
                scoringAttempt != null;

        boolean skipped =
                !answered
                        && isPermanentlySkipped(
                        question,
                        attempts
                );

        boolean notAttempted =
                !answered && !skipped;

        boolean retried =
                wasRetried(
                        question,
                        attempts
                );

        Integer scoreUsedForOverall =
                scoringAttempt != null
                        ? scoringAttempt.getScore()
                        : null;

        return new CustomPracticeQuestionReportDto(
                question.getId(),
                question.getQuestionOrder(),
                question.getQuestionText(),
                answered,
                skipped,
                notAttempted,
                retried,
                scoreUsedForOverall,
                attemptDtos
        );
    }

    // =========================================================
    // ATTEMPT DTO
    // =========================================================

    private CustomPracticeAttemptDto mapAttempt(
            CustomPracticeAttempt attempt) {

        return new CustomPracticeAttemptDto(
                attempt.getId(),
                attempt.getAttemptNumber(),
                attempt.getAttemptType(),
                attempt.getUserAnswer(),
                attempt.getEvaluationStatus(),
                attempt.getScore(),
                attempt.getFeedback(),
                attempt.getSubmittedAt()
        );
    }

    // =========================================================
    // SCORING ATTEMPT
    // =========================================================

    /**
     * Final scoring rule:
     *
     * If a question has only one submitted answer, that answer
     * determines the result.
     *
     * If a question was answered again during Round 2, the most
     * recent submitted answer determines the final question result
     * and the score used in the overall report.
     *
     * Examples:
     *
     * Round 1 INCORRECT (30)
     * Round 2 CORRECT   (90)
     * Final score used = 90
     *
     * Round 1 skipped
     * Round 2 CORRECT   (80)
     * Final score used = 80
     */
    private CustomPracticeAttempt getScoringAttempt(
            List<CustomPracticeAttempt> attempts) {

        if (attempts == null
                || attempts.isEmpty()) {

            return null;
        }

        return attempts.get(
                attempts.size() - 1
        );
    }

    // =========================================================
    // RETRY DETECTION
    // =========================================================

    private boolean wasRetried(
            CustomPracticeQuestion question,
            List<CustomPracticeAttempt> attempts) {

        if (attempts.size() > 1) {
            return true;
        }

        /*
         * Round 1 may have been skipped, meaning the Round 2
         * submission is the only stored attempt.
         */
        if (Boolean.TRUE.equals(
                question.getFirstRoundSkipped())
                && !attempts.isEmpty()) {

            return true;
        }

        /*
         * Covers:
         * Round 1 skip -> Round 2 skip.
         */
        return Boolean.TRUE.equals(
                question.getFirstRoundSkipped())
                && Boolean.TRUE.equals(
                question.getRetryCompleted());
    }

    // =========================================================
    // SKIPPED DETECTION
    // =========================================================

    private boolean isPermanentlySkipped(
            CustomPracticeQuestion question,
            List<CustomPracticeAttempt> attempts) {

        return Boolean.TRUE.equals(
                question.getFirstRoundSkipped())
                && Boolean.TRUE.equals(
                question.getRetryCompleted())
                && attempts.isEmpty();
    }

    // =========================================================
    // PRACTICE SUMMARY FOR GROQ
    // =========================================================

    private String buildPracticeSummary(
            CustomPracticeSession session,
            List<CustomPracticeQuestion> questions,
            ReportStatistics statistics,
            double overallScore) {

        StringBuilder summary =
                new StringBuilder();

        summary.append("Session: ")
                .append(session.getSessionName())
                .append("\n");

        summary.append("Session Status: ")
                .append(session.getStatus())
                .append("\n");

        summary.append("Total Questions: ")
                .append(session.getTotalQuestions())
                .append("\n");

        summary.append("Answered Questions: ")
                .append(
                        statistics
                                .answeredQuestions()
                )
                .append("\n");

        summary.append("Correct: ")
                .append(
                        statistics
                                .correctQuestions()
                )
                .append("\n");

        summary.append("Partially Correct: ")
                .append(
                        statistics
                                .partiallyCorrectQuestions()
                )
                .append("\n");

        summary.append("Incorrect: ")
                .append(
                        statistics
                                .incorrectQuestions()
                )
                .append("\n");

        summary.append("Skipped: ")
                .append(
                        statistics
                                .skippedQuestions()
                )
                .append("\n");

        summary.append("Not Attempted: ")
                .append(
                        statistics
                                .notAttemptedQuestions()
                )
                .append("\n");

        summary.append("Retried Questions: ")
                .append(
                        statistics
                                .retriedQuestions()
                )
                .append("\n");

        summary.append("Overall Score: ")
                .append(overallScore)
                .append("/100\n\n");

        summary.append(
                "Question Results:\n"
        );

        for (CustomPracticeQuestion question :
                questions) {

            List<CustomPracticeAttempt> attempts =
                    getAttempts(question);

            summary.append("\nQuestion ")
                    .append(
                            question
                                    .getQuestionOrder()
                    )
                    .append(": ")
                    .append(
                            question
                                    .getQuestionText()
                    )
                    .append("\n");

            if (attempts.isEmpty()) {

                if (isPermanentlySkipped(
                        question,
                        attempts)) {

                    summary.append(
                            "Final Result: SKIPPED\n"
                    );

                } else {

                    summary.append(
                            "Final Result: NOT_ATTEMPTED\n"
                    );
                }

                continue;
            }

            for (CustomPracticeAttempt attempt :
                    attempts) {

                summary.append("Attempt ")
                        .append(
                                attempt
                                        .getAttemptNumber()
                        )
                        .append(" (")
                        .append(
                                attempt
                                        .getAttemptType()
                        )
                        .append("): ")
                        .append(
                                attempt
                                        .getEvaluationStatus()
                        )
                        .append(", Score ")
                        .append(
                                attempt
                                        .getScore()
                        )
                        .append("/100")
                        .append("\n");

                summary.append("Feedback: ")
                        .append(
                                attempt
                                        .getFeedback()
                        )
                        .append("\n");
            }

            CustomPracticeAttempt finalAttempt =
                    getScoringAttempt(attempts);

            summary.append(
                            "Final Result Used For Report: "
                    )
                    .append(
                            finalAttempt
                                    .getEvaluationStatus()
                    )
                    .append(", Score ")
                    .append(
                            finalAttempt
                                    .getScore()
                    )
                    .append("/100\n");
        }

        return summary.toString();
    }

    // =========================================================
    // DURATION
    // =========================================================

    private Long calculatePracticeDurationSeconds(
            CustomPracticeSession session) {

        if (session.getStartedAt() == null) {
            return 0L;
        }

        LocalDateTime finishedAt =
                switch (session.getStatus()) {

                    case COMPLETED ->
                            session.getCompletedAt();

                    case ENDED ->
                            session.getEndedAt();

                    case EXPIRED ->
                            session.getExpiredAt();

                    default ->
                            null;
                };

        if (finishedAt == null) {
            return 0L;
        }

        long seconds =
                Duration.between(
                        session.getStartedAt(),
                        finishedAt
                ).getSeconds();

        return Math.max(
                seconds,
                0L
        );
    }

    // =========================================================
    // REPORTABLE STATUS
    // =========================================================

    private boolean isReportableStatus(
            CustomPracticeStatus status) {

        return status
                == CustomPracticeStatus.COMPLETED

                || status
                == CustomPracticeStatus.ENDED

                || status
                == CustomPracticeStatus.EXPIRED;
    }

    // =========================================================
    // DATABASE HELPERS
    // =========================================================

    private List<CustomPracticeQuestion>
    getOrderedQuestions(
            CustomPracticeSession session) {

        return questionRepository
                .findAllBySessionOrderByQuestionOrderAsc(
                        session
                );
    }

    private List<CustomPracticeAttempt>
    getAttempts(
            CustomPracticeQuestion question) {

        return attemptRepository
                .findAllByQuestionOrderByAttemptNumberAsc(
                        question
                );
    }

    // =========================================================
    // FALLBACK FEEDBACK
    // =========================================================

    private String buildFallbackFeedback() {

        return "Overall AI feedback is temporarily unavailable. "
                + "Your question-by-question scores and feedback "
                + "remain available in this report.";
    }

    // =========================================================
    // AUTHENTICATED USER
    // =========================================================

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
    // INTERNAL REPORT STATISTICS
    // =========================================================

    private record ReportStatistics(
            int answeredQuestions,
            int correctQuestions,
            int partiallyCorrectQuestions,
            int incorrectQuestions,
            int skippedQuestions,
            int notAttemptedQuestions,
            int retriedQuestions
    ) {
    }
}