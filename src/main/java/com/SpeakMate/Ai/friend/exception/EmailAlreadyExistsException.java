package com.SpeakMate.Ai.friend.exception;

public class EmailAlreadyExistsException
        extends RuntimeException {

    public EmailAlreadyExistsException(
            String message
    ) {
        super(message);
    }
}