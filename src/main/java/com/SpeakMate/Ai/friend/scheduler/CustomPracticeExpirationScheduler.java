package com.SpeakMate.Ai.friend.scheduler;

import com.SpeakMate.Ai.friend.entities.CustomPracticeSession;
import com.SpeakMate.Ai.friend.enumeration.CustomPracticeStatus;
import com.SpeakMate.Ai.friend.repository.CustomPracticeSessionRepository;
import com.SpeakMate.Ai.friend.service.CustomPracticeReportService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class CustomPracticeExpirationScheduler {

    private static final long EXPIRATION_CHECK_INTERVAL =
            60 * 60 * 1000L;

    private final CustomPracticeSessionRepository sessionRepository;
    private final CustomPracticeReportService reportService;

    public CustomPracticeExpirationScheduler(
            CustomPracticeSessionRepository sessionRepository,
            CustomPracticeReportService reportService) {

        this.sessionRepository = sessionRepository;
        this.reportService = reportService;
    }

    // =========================================================
    // EXPIRE PAUSED SESSIONS
    // =========================================================

    @Scheduled(fixedDelay = EXPIRATION_CHECK_INTERVAL)
    @Transactional
    public void expirePausedSessions() {

        LocalDateTime now =
                LocalDateTime.now();

        /*
         * This first query only finds possible expiration
         * candidates.
         *
         * We do not modify these returned entities directly.
         * Each candidate is reloaded below using a
         * PESSIMISTIC_WRITE lock.
         */
        List<CustomPracticeSession> candidates =
                sessionRepository
                        .findAllByStatusAndExpiresAtBefore(
                                CustomPracticeStatus.PAUSED,
                                now
                        );

        for (CustomPracticeSession candidate :
                candidates) {

            processExpirationCandidate(
                    candidate.getId()
            );
        }
    }

    // =========================================================
    // PROCESS ONE CANDIDATE
    // =========================================================

    private void processExpirationCandidate(
            Long sessionId) {

        if (sessionId == null) {
            return;
        }

        CustomPracticeSession session =
                sessionRepository
                        .findByIdForUpdate(sessionId)
                        .orElse(null);

        if (session == null) {
            return;
        }

        /*
         * The session may have been resumed, completed,
         * manually ended, or otherwise changed after the
         * candidate query ran.
         */
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

        /*
         * A terminal session should no longer carry pause
         * metadata that suggests it can still be resumed.
         */
        session.setPausedAt(null);

        CustomPracticeSession savedSession =
                sessionRepository.save(session);

        // =====================================================
        // REPORT GENERATION
        // =====================================================

        try {

            reportService.generateAndStoreReport(
                    savedSession
            );

        } catch (Exception e) {

            /*
             * Session expiration itself must remain successful
             * even when AI report generation temporarily fails.
             *
             * CustomPracticeReportService#getReport() can
             * generate/store the missing report later.
             */
            System.err.println(
                    "Unable to generate report for expired "
                            + "custom practice session "
                            + savedSession.getId()
                            + ": "
                            + e.getMessage()
            );
        }
    }
}