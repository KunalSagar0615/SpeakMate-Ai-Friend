package com.SpeakMate.Ai.friend.mapper;

import com.SpeakMate.Ai.friend.dto.UserDto;
import com.SpeakMate.Ai.friend.dto.UserProfileDto;
import com.SpeakMate.Ai.friend.entities.User;

public class UserMapper {

    public static UserDto toDto(User user) {

        return new UserDto(
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getMobileNumber(),
                user.getEmail(),
                user.getRole()
        );
    }

    public static UserProfileDto toProfileDto(User user) {
        return new UserProfileDto(
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getEmail(),
                user.getMobileNumber(),
                user.getCountry(),
                user.getHighestEducation(),
                user.getCurrentOccupation(),
                user.getEmailVerified(),
                user.getRole()
        );
    }

    public static User toEntity(UserDto dto) {

        User user = new User();

        user.setId(dto.getId());
        user.setName(dto.getName());
        user.setUsername(dto.getUsername());
        user.setMobileNumber(dto.getMobileNumber());
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());

        return user;
    }
}