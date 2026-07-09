package com.SpeakMate.Ai.friend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponseDto {

    private String token;

    private String username;

    private String name;

    private String role;
}
