package com.SpeakMate.Ai.friend.entities;

import com.SpeakMate.Ai.friend.enumeration.AnswerEvaluationStatus;
import com.SpeakMate.Ai.friend.enumeration.PracticeAttemptType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "custom_practice_attempts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_custom_question_attempt_number",
                        columnNames = {"question_id", "attempt_number"}
                )
        }
)
public class CustomPracticeAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer attemptNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PracticeAttemptType attemptType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String userAnswer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnswerEvaluationStatus evaluationStatus;

    @Column(nullable = false)
    private Integer score;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String feedback;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private CustomPracticeQuestion question;

    @PrePersist
    public void prePersist() {
        if (submittedAt == null) {
            submittedAt = LocalDateTime.now();
        }
    }
}