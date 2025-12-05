package com.vbote.api.domain.exception;

public class UserBlockedException extends DomainException{

    public UserBlockedException(String username) {
        super("User is blocked: " + username);
    }
}
