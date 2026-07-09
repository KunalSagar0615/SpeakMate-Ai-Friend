package com.SpeakMate.Ai.friend.mapper;

import com.SpeakMate.Ai.friend.dto.UserDto;
import com.SpeakMate.Ai.friend.dto.UserProfileDto;
import com.SpeakMate.Ai.friend.entities.User;

public class UserMapper {

    public static UserDto toDto(User user) {

        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPassword(),
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
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        user.setRole(dto.getRole());

        return user;
    }
}