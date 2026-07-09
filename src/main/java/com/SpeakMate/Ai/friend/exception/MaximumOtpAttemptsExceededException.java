package com.SpeakMate.Ai.friend.exception;

public class MaximumOtpAttemptsExceededException
        extends RuntimeException {

    public MaximumOtpAttemptsExceededException(
            String message
    ) {
        super(message);
    }
}