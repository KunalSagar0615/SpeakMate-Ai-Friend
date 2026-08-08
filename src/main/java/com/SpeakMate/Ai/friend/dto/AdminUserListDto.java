package com.SpeakMate.Ai.friend.dto;

import com.SpeakMate.Ai.friend.enumeration.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserListDto {

    private Long id;

    private String name;

    private String email;

    private String mobileNumber;
}
