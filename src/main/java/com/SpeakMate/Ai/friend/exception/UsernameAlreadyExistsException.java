package com.SpeakMate.Ai.friend.exception;

public class UsernameAlreadyExistsException
        extends RuntimeException {

    public UsernameAlreadyExistsException(
            String message
    ) {
        super(message);
    }
}