package com.SpeakMate.Ai.friend.dto;

import com.SpeakMate.Ai.friend.enumeration.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDetailsDto {

    private Long id;

    private String name;

    private String username;

    private String email;

    private String mobileNumber;

    private String country;

    private String highestEducation;

    private String currentOccupation;

    private Role role;

    private Boolean emailVerified;

    private LocalDateTime createdAt;

    private List<UserSessionSummaryDto> sessions;
}
