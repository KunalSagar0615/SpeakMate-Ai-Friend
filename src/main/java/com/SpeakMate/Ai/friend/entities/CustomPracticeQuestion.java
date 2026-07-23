package com.SpeakMate.Ai.friend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "custom_practice_questions")
public class CustomPracticeQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(nullable = false)
    private Integer questionOrder;

    @Column(columnDefinition = "TEXT")
    private String draftAnswer;

    @Column(nullable = false)
    private Boolean firstRoundSkipped;

    @Column(nullable = false)
    private Boolean retryRequired;

    @Column(nullable = false)
    private Boolean retryCompleted;

    @Column(nullable = false)
    private Boolean completed;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private CustomPracticeSession session;

    @OneToMany(
            mappedBy = "question",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("attemptNumber ASC")
    private List<CustomPracticeAttempt> attempts = new ArrayList<>();

    @PrePersist
    public void prePersist() {

        if (firstRoundSkipped == null) {
            firstRoundSkipped = false;
        }

        if (retryRequired == null) {
            retryRequired = false;
        }

        if (retryCompleted == null) {
            retryCompleted = false;
        }

        if (completed == null) {
            completed = false;
        }
    }
}