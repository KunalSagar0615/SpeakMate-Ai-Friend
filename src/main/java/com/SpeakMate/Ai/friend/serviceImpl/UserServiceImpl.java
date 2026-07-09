package com.SpeakMate.Ai.friend.serviceImpl;

import com.SpeakMate.Ai.friend.dto.UserDto;
import com.SpeakMate.Ai.friend.entities.User;
import com.SpeakMate.Ai.friend.exception.ResourceNotFoundException;
import com.SpeakMate.Ai.friend.mapper.UserMapper;
import com.SpeakMate.Ai.friend.repository.UserRepository;
import com.SpeakMate.Ai.friend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDto createUser(UserDto userDto) {

        User user = UserMapper.toEntity(userDto);

        User savedUser = userRepository.save(user);

        return UserMapper.toDto(savedUser);
    }

    @Override
    public UserDto getUserById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id : " + id));

        return UserMapper.toDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(UserMapper::toDto)
                .toList();
    }

    @Override
    public UserDto updateUser(Long id, UserDto userDto) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id : " + id));

        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setPassword(userDto.getPassword());
        user.setRole(userDto.getRole());

        User updatedUser = userRepository.save(user);

        return UserMapper.toDto(updatedUser);
    }

    @Override
    public void deleteUserById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id : " + id));

        userRepository.delete(user);
    }
}