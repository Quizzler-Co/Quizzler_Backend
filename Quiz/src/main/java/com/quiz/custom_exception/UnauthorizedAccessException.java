package com.quiz.custom_exception;

public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String s) {
        super(s);
    }
}
