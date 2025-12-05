package com.vbote.api.domain.exception;

public class UserAlreadyExistsException extends DomainException{

    public UserAlreadyExistsException(String username) {
        super("User already exists with username: " + username);
    }
}
