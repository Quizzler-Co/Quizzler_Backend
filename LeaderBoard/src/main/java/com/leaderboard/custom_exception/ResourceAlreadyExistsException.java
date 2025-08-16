package com.leaderboard.custom_exception;

public class ResourceAlreadyExistsException extends RuntimeException {
    public ResourceAlreadyExistsException(String msg){
        super(msg);
    }
}
