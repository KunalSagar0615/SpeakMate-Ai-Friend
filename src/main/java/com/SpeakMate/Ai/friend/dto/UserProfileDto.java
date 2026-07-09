package com.SpeakMate.Ai.friend.dto;

import com.SpeakMate.Ai.friend.enumeration.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {

    private Long id;
    private String name;
    private String username;
    private String email;
    private String mobileNumber;
    private String country;
    private String highestEducation;
    private String currentOccupation;
    private Boolean emailVerified;
    private Role role;
}
