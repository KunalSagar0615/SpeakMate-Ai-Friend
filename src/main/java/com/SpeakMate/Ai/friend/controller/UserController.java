package com.SpeakMate.Ai.friend.controller;

import com.SpeakMate.Ai.friend.dto.UserDto;
import com.SpeakMate.Ai.friend.exception.ApiResponse;
import com.SpeakMate.Ai.friend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/create-user")
    public ResponseEntity<UserDto> createUser(
            @Valid @RequestBody UserDto userDto) {

        UserDto createdUser = userService.createUser(userDto);

        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @GetMapping("/get-user-by-id/{id}")
    public ResponseEntity<UserDto> getUserById(
            @PathVariable Long id) {

        UserDto userDto = userService.getUserById(id);

        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/get-all-users")
    public ResponseEntity<List<UserDto>> getAllUsers() {

        List<UserDto> users = userService.getAllUsers();

        return ResponseEntity.ok(users);
    }

    @PutMapping("/update-user-by-id/{id}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDto userDto) {

        UserDto updatedUser =
                userService.updateUser(id, userDto);

        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/delete-user-by-id/{id}")
    public ResponseEntity<ApiResponse> deleteUserById(
            @PathVariable Long id) {

        userService.deleteUserById(id);

        ApiResponse response =
                new ApiResponse(
                        "User deleted successfully",
                        true);

        return ResponseEntity.ok(response);
    }
}