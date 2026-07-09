package com.SpeakMate.Ai.friend.service;

import com.SpeakMate.Ai.friend.dto.UserDto;

import java.util.List;

public interface UserService {

    UserDto createUser(UserDto userDto);

    UserDto getUserById(Long id);

    List<UserDto> getAllUsers();

    UserDto updateUser(Long id, UserDto userDto);

    void deleteUserById(Long id);
}