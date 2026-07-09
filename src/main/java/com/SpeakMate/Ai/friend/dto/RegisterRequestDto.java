package com.SpeakMate.Ai.friend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDto {
    private String name;
    private String username;
    private String email;
    private String password;
    private String mobileNumber;
    private String highestEducation;
    private String currentOccupation;
    private String country;
}
