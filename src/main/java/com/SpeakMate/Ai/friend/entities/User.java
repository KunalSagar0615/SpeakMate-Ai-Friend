package com.SpeakMate.Ai.friend.entities;

import com.SpeakMate.Ai.friend.enumeration.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String mobileNumber;

    private String highestEducation;

    private String currentOccupation;

    private String country;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user")
    private List<Session> sessions;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();

        if (emailVerified == null) {
            emailVerified = false;
        }
    }

    private Boolean emailVerified;
}