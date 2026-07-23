package com.SpeakMate.Ai.friend.entities;

import com.SpeakMate.Ai.friend.enumeration.CustomPracticeStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "custom_practice_sessions")
public class CustomPracticeSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String sessionName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomPracticeStatus status;

    @Column(nullable = false)
    private Integer totalQuestions;

    @Column(nullable = false)
    private Integer currentQuestionIndex;

    @Column(nullable = false)
    private Integer currentRound;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime pausedAt;

    private LocalDateTime expiresAt;

    private LocalDateTime completedAt;

    private LocalDateTime endedAt;

    private LocalDateTime expiredAt;

    private Double overallScore;

    @Column(columnDefinition = "TEXT")
    private String overallAiFeedback;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(
            mappedBy = "session",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("questionOrder ASC")
    private List<CustomPracticeQuestion> questions = new ArrayList<>();

    @PrePersist
    public void prePersist() {

        if (status == null) {
            status = CustomPracticeStatus.ACTIVE;
        }

        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }

        if (currentQuestionIndex == null) {
            currentQuestionIndex = 0;
        }

        if (currentRound == null) {
            currentRound = 1;
        }
    }
}