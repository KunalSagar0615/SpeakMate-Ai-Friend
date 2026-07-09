package com.SpeakMate.Ai.friend.exception;

import com.SpeakMate.Ai.friend.repository.UserRepository;

public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException(String message) {
        super(message);
    }
}
